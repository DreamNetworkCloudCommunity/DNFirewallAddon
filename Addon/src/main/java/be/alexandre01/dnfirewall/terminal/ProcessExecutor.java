package be.alexandre01.dnfirewall.terminal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ProcessExecutor {

    String command;
    InputMode inputMode;
    public ProcessExecutor(String command,InputMode inputMode) {
        this.command = command;
        this.inputMode = inputMode;
    }

    public void executeToEnd(ProcessEndListener listener) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);
        ProcessBuilder.Redirect redirect = null;

        try {
            Process process = processBuilder.start();
            if(inputMode == InputMode.ERR) {
                List<String> lines = new ArrayList<>();
                readInput(lines,process.getErrorStream(),()->{
                    listener.onProcessRead(lines.toArray(new String[0]));
                });
            }
            if(inputMode == InputMode.IN) {
                List<String> lines = new ArrayList<>();
                readInput(lines,process.getInputStream(),()->{
                    listener.onProcessRead(lines.toArray(new String[0]));
                });
            }
            if(inputMode == InputMode.MIXED){
                List<String> lines = new ArrayList<>();

                Runnable runnable = new Runnable() {
                    int i = 0;
                    @Override
                    public void run() {
                        if(i == 0){
                            i++;
                            return;
                        }
                        listener.onProcessRead(lines.toArray(new String[0]));
                    }
                };
                readInput(lines,process.getInputStream(),runnable);
                readInput(lines,process.getErrorStream(),runnable);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void readInput(List<String> lines,InputStream inputStream,Runnable runnable){
        new Thread(){
            @Override
            public void run() {
                if(inputMode == InputMode.ERR) {

                    byte[] buffer = new byte[1024];
                    int read;
                    try {
                        while ((read = inputStream.read(buffer)) > 0) {
                            String line = new String(buffer, 0, read);
                            lines.add(line);
                        }
                        runnable.run();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();
    }


    public interface ProcessEndListener {
        void onProcessRead(String[] lines);
        void onProcessComplete(int exitValue);
    }

    public enum InputMode {
        IN, ERR, MIXED;
    }

}
