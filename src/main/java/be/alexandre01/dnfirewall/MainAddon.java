package be.alexandre01.dnfirewall;

import be.alexandre01.dnfirewall.listener.ProxyLifecycleListener;
import be.alexandre01.dnfirewall.terminal.ProcessExecutor;
import be.alexandre01.dnfirewall.terminal.RulesFactory;
import be.alexandre01.dreamnetwork.api.addons.Addon;
import be.alexandre01.dreamnetwork.api.addons.DreamExtension;
import be.alexandre01.dreamnetwork.core.console.Console;
import lombok.Getter;
import org.apache.http.client.methods.Configurable;

public class MainAddon extends DreamExtension {
    public MainAddon(Addon addon){
        super(addon);
    }
    private static String OS = System.getProperty("os.name").toLowerCase();

   @Getter
   private RulesFactory rulesFactory = new RulesFactory();

    @Override
    public void onLoad(){
        // If you want to compile it with Plugins in ressource use "mvn clean install -P PlugInAddon" instead of "mvn clean install"
        // Remove the comment if you want to register a plugin to the servers
        // -> "//" registerPluginToServers( MainAddon.class.getClassLoader().getResourceAsStream("Plugins-1.0-SNAPSHOT.jar"),"YourPlugin.jar");
    }

    @Override
    public void start(){

        if(OS.contains("win")) {
            System.out.println("You are on Windows, the firewall addon is not supported");
        }
        else if(OS.contains("nix") || OS.contains("nux") || OS.contains("aix") || OS.contains("mac")) {
            System.out.println("You are on Linux, the firewall addon is supported");
            System.out.println("searching UFW");

            ProcessExecutor processExecutor = new ProcessExecutor("ufw status", ProcessExecutor.InputMode.IN);
            processExecutor.executeToEnd(new ProcessExecutor.ProcessEndListener() {
                @Override
                public void onProcessRead(String[] lines) {
                    if(lines.length == 0){
                        System.out.println("No return of ufw");
                        return;
                    }
                    if(lines[0].contains("Status: active")){
                        System.out.println("Firewall -> Everything is OK. UFW is already enabled !");
                    }else if(lines[0].contains("Status: inactive")){
                        System.out.println("Firewall -> UFW is disabled");
                        System.out.println("Adding ssh port and Enabling UFW ");
                        // adding ssh rule to ufw (need to create a config file later)
                        rulesFactory.addRule("ssh",new RulesFactory.Rule("ssh"));
                        rulesFactory.enableUFW();
                    }
                  /*  for (String line : lines) {
                        Console.debugPrint(line);
                    }*/
                    getDnCoreAPI().getEventsFactory().registerListener(new ProxyLifecycleListener(MainAddon.this));
                }
            });

        } else {
            System.out.println("You are on an unknown OS, the firewall addon is not supported");
        }
    }

}