package com.rasturize.anticheat.storage;

import com.rasturize.anticheat.api.check.wrapper.CheckWrapper;
import com.rasturize.anticheat.api.violation.Violation;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.rasturize.anticheat.Apex;
import com.rasturize.anticheat.handler.CheckHandler;
import com.rasturize.anticheat.utils.Utils;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoStorage implements Storage {
	private final CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
			fromProviders(PojoCodecProvider.builder().automatic(true).build()));
	private MongoCollection<Violation> loggedViolations;
	private MongoCollection<CheckWrapper> settings;
	private MongoCollection<ConfigEntry> config;

	private ConcurrentLinkedQueue<Violation> violations = new ConcurrentLinkedQueue<>();
	public boolean updateSettings = true;

	public String host, database;

	public MongoStorage() {
		host = Apex.instance.getConfig().getString("storage.host");
		database = Apex.instance.getConfig().getString("storage.database");
	}

	public MongoStorage(String host, String database) {
		this.host = host;
		this.database = database;
	}

	@Override
	public String getString(String key) {
		ConfigEntry entry = config.find(eq("key", key)).first();
		return entry == null ? null : entry.value;
	}

	@Override
	public void set(String key, Object value) {
		config.deleteOne(eq("key", key));
		config.insertOne(new ConfigEntry(key, value.toString()));
	}

	@Override
	public void init() {
		MongoClient client = new MongoClient(new ServerAddress(host, 27017), MongoClientOptions.builder().codecRegistry(pojoCodecRegistry).build());
		MongoDatabase mongodb = client.getDatabase(database);

		loggedViolations = mongodb.getCollection("violations", Violation.class);
		settings = mongodb.getCollection("settings", CheckWrapper.class);
		config = mongodb.getCollection("config", ConfigEntry.class);

		if (updateSettings) {
			List<String> existing = new ArrayList<>();
			settings.find().forEach(((Consumer<CheckWrapper>) setting -> {
				existing.add(setting.id());
			}));

			CheckHandler.getWrappers().values().stream().filter(type -> !existing.contains(type.id())).forEach(this::updateValue);

			new Thread(() -> {
				while (Apex.instance != null && Apex.instance.isEnabled()) {
					try {
						settings.find().forEach((Consumer<CheckWrapper>) CheckHandler::put);
						Utils.sleep(TimeUnit.MINUTES.toMillis(1));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, "ApexMongoConfigUpdater").start();
		}
		new Thread(() -> {
			while (Apex.instance != null && Apex.instance.isEnabled()) {
				try {
					Utils.sleep(1000);
					if (violations.isEmpty()) continue;
					try {
						loggedViolations.insertMany(new ArrayList<>(violations));
					} catch (Exception e) {
						e.printStackTrace();
					}
					violations.clear();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, "ApexMongoCommitter").start();
	}

	@Override
	public void updateValue(CheckWrapper type) {
		settings.deleteOne(eq("_id", type.id()));
		settings.insertOne(type);
	}

	@Override
	public void addAlert(Violation violation) {
		violations.add(violation);
	}

	@Override
	public List<Violation> getViolations(UUID uuid, CheckWrapper type, int page, int limit, long from, long to) {
		List<Violation> violations = new ArrayList<>();
		loggedViolations.find(eq("player", uuid)).skip(page * limit).limit(limit).sort(new Document("time", -1)).forEach((Consumer<? super Violation>) violations::add);
		return violations;
	}

	@Override
	public Map<CheckWrapper, Integer> getHighestViolations(UUID uuid, CheckWrapper type, long from, long to) {
		FindIterable<Violation> vls = loggedViolations.find(eq("player", uuid)).sort(new Document("time", -1));

		if (vls.first() == null) return null;

		Map<CheckWrapper, Integer> counts = new HashMap<>();

		for (Violation v : vls) {
			Integer old = counts.get(CheckHandler.getWrapper(v.type));
			if (old == null) old = 0;
			counts.put(CheckHandler.getWrapper(v.type), old + 1);
		}
		return counts;
	}
}
