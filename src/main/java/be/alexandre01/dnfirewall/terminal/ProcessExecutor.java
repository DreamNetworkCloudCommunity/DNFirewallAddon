package be.alexandre01.dnfirewall.terminal;

import be.alexandre01.dreamnetwork.core.console.Console;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ProcessExecutor {

    String command;
    InputMode inputMode;
    public ProcessExecutor(String command,InputMode inputMode) {
        this.command = command;
        this.inputMode = inputMode;
    }

    public ProcessBuilder createProcessBuilder(){
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("/bin/bash", "-c", "sudo -S "+command);
        return processBuilder;
    }



    public void executeToEnd(ProcessEndListener listener) {
        try {
            ProcessBuilder processBuilder = createProcessBuilder();
            if(inputMode == InputMode.ERR) {
                Process process = processBuilder.start();
                List<String> lines = new ArrayList<>();
                readInputToEnd(lines,process.getErrorStream(),()->{
                    if(listener != null){
                        listener.onProcessRead(lines.toArray(new String[0]));
                    }
                });
            }
            if(inputMode == InputMode.IN) {
                Process process = processBuilder.start();
                List<String> lines = new ArrayList<>();
                readInputToEnd(lines,process.getInputStream(),()->{
                    if(listener != null){
                        listener.onProcessRead(lines.toArray(new String[0]));
                    }
                });
            }
            if(inputMode == InputMode.MIXED){
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();
                List<String> lines = new ArrayList<>();

                Runnable runnable = new Runnable() {
                    int i = 0;
                    @Override
                    public void run() {
                        if(i == 0){
                            i++;
                            return;
                        }
                        if(listener != null){
                            listener.onProcessRead(lines.toArray(new String[0]));
                        }
                    }
                };
                readInputToEnd(lines,process.getInputStream(),runnable);
                readInputToEnd(lines,process.getErrorStream(),runnable);
            }
        } catch (IOException e) {
            Console.bug(e);
        }

    }

    public void readInputToEnd(List<String> lines,InputStream inputStream,Runnable runnable){
        readInput(inputStream, new Consumer<String>() {
            @Override
            public void accept(String string) {
                lines.add(string);
            }
        }, runnable);
    }
    public void readInput(InputStream inputStream, Consumer<String> consumer, Runnable end){
        new Thread(){
            @Override
            public void run() {
                byte[] buffer = new byte[1024];
                int read;
                try {
                    while ((read = inputStream.read(buffer)) > 0) {
                        consumer.accept(new String(buffer, 0, read));
                    }
                    end.run();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    public interface ProcessEndListener {
        void onProcessRead(String[] lines);
    }
    public interface ProcessListener {
        void onProcessRead(String line);
    }

    public enum InputMode {
        IN, ERR, MIXED;
    }

}
