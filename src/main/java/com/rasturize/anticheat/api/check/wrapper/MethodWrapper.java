package com.rasturize.anticheat.api.check.wrapper;

import com.rasturize.anticheat.Apex;
import com.rasturize.anticheat.ApexCommand;
import com.rasturize.anticheat.api.check.Check;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Method;

@Getter
@AllArgsConstructor
public class MethodWrapper {
    private Check check;
    private Method method;
    private int priority;

    public void call(Object argument) throws Exception {
        if (check.check == null || check.check.alert() || check.check.cancel() || check.check.ban() || Apex.devServer) {
            if (method.getParameterTypes()[0] == argument.getClass()) {
                long start = System.nanoTime();
                method.invoke(check, argument);

                ApexCommand.profiler.stopCustom(argument.getClass().getSimpleName().replace("WrappedPacket", "").replace("Event", ""), System.nanoTime() - start);
            }
        }
    }
}
