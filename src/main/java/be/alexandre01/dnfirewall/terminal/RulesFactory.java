package be.alexandre01.dnfirewall.terminal;

import be.alexandre01.dreamnetwork.core.console.Console;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;

/*
 ↬   Made by Alexandre01Dev 😎
 ↬   done on 05/10/2023 at 12:36
*/
public class RulesFactory {
    final HashMap<String,Rule> rules = new HashMap<>();

    public void addRule(String name, Rule rule){
        rules.put(name,rule);

        ProcessExecutor processExecutor = new ProcessExecutor("ufw allow "+rule.getPortName(), ProcessExecutor.InputMode.MIXED);
        processExecutor.executeToEnd(new ProcessExecutor.ProcessEndListener() {
            @Override
            public void onProcessRead(String[] lines) {
                if(lines.length == 0){
                    Console.print("No return of UFW.", Level.SEVERE);
                    return;
                }
                if(!(lines[0].contains("Rule added") || lines[0].contains("Skipping adding existing rule") || lines[0].contains("Rules updated"))){
                    Console.print("Didn't detect if the UFW rule has been added");
                    for (String string : lines) {
                        Console.debugPrint("=> "+string);
                    }
                }
                System.out.println("An UFW rule has been added to port "+ rule.getPortName());
            }
        });
    }

    public void removeRule(String name){
        String port = rules.get(name).getPortName();
        rules.remove(name);
        ProcessExecutor processExecutor = new ProcessExecutor("ufw delete allow "+port, ProcessExecutor.InputMode.MIXED);
        processExecutor.executeToEnd(new ProcessExecutor.ProcessEndListener() {
            @Override
            public void onProcessRead(String[] lines) {
                if(lines.length == 0){
                    Console.print("No return of UFW.", Level.SEVERE);
                    return;
                }
                if(!(lines[0].contains("Rule removed") || lines[0].contains("Skipping removing existing rule"))){
                    Console.print("Didn't detect if the UFW rule has been added");
                    for (String string : lines) {
                        System.out.println("=> "+string);
                    }
                }
                System.out.println("An UFW rule has been removed to port "+ port);
            }
        });
    }

    public void enableUFW(){
        ProcessExecutor enable = new ProcessExecutor("ufw enable", ProcessExecutor.InputMode.IN);
        ProcessBuilder processBuilder = enable.createProcessBuilder();
        try {
            Process process = processBuilder.start();
            AtomicBoolean success = new AtomicBoolean(false);
            enable.readInput(process.getInputStream(), new Consumer<String>() {

                @Override
                public void accept(String string) {
                    if(string.contains("Command may disrupt existing ssh connections. Proceed with operation (y|n)?")){
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                        try {
                            writer.write("y");
                            writer.flush();
                            writer.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if(string.contains("Firewall is active and enabled on system startup")){
                        System.out.println("Operation succeed ! UFW has been enable");
                        success.set(true);
                    }
                }
            },() -> {
                if(!success.get()){
                    Console.print("DNFirewall addon does not detect whether UFW had been started. Please enable it manually",Level.FINE);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @AllArgsConstructor @Getter @Setter
    public static class Rule {
        private String portName;
    }
}
