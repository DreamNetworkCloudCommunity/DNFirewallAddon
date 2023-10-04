package be.alexandre01.dnfirewall;

import be.alexandre01.dnfirewall.terminal.ProcessExecutor;
import be.alexandre01.dreamnetwork.api.addons.Addon;
import be.alexandre01.dreamnetwork.api.addons.DreamExtension;
import org.apache.http.client.methods.Configurable;

public class MainAddon extends DreamExtension {
    public MainAddon(Addon addon){
        super(addon);
    }
    private static String OS = System.getProperty("os.name").toLowerCase();

    @Override
    public void onLoad(){
        // If you want to compile it with Plugins in ressource use "mvn clean install -P PlugInAddon" instead of "mvn clean install"
        // Remove the comment if you want to register a plugin to the servers
        // -> "//" registerPluginToServers( MainAddon.class.getClassLoader().getResourceAsStream("Plugins-1.0-SNAPSHOT.jar"),"YourPlugin.jar");
    }

    @Override
    public void start(){
        System.out.println("UFW search");
        if(OS.contains("win")) {
            System.out.println("You are on Windows, the firewall addon is not supported");
        }
        else if(OS.contains("nix") || OS.contains("nux") || OS.contains("aix")) {
            System.out.println("You are on Linux, the firewall addon is supported");
            System.out.println("UFW search");
            ProcessExecutor processExecutor = new ProcessExecutor("ufw status", ProcessExecutor.InputMode.ERR);
            processExecutor.executeToEnd(new ProcessExecutor.ProcessEndListener() {
                @Override
                public void onProcessRead(String[] lines) {
                    System.out.println("UFW found");
                    System.out.println("UFW status : "+lines[0]);
                    if(lines[0].contains("active")){
                        System.out.println("UFW is enabled");
                    }else if(lines[0].contains("inactive")){
                        System.out.println("UFW is disabled");
                    }
                    //print process output
                    for (String line : lines) {
                        System.out.println(line);
                    }
                }

                @Override
                public void onProcessComplete(int exitValue) {
                    System.out.println("END PROCESS");
                    System.out.println("UFW found");
                    System.out.println("UFW status : "+exitValue);
                    if(exitValue == 0){
                        System.out.println("UFW is enabled");
                    }else if(exitValue == 1){
                        System.out.println("UFW is disabled");
                    }
                }
            });
        }
        else if(OS.contains("mac")) {
            System.out.println("You are on Mac, the firewall addon is not supported");
        }
        else {
            System.out.println("You are on an unknown OS, the firewall addon is not supported");
        }
    }

}