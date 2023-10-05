package be.alexandre01.dnfirewall.listener;

import be.alexandre01.dnfirewall.MainAddon;
import be.alexandre01.dnfirewall.terminal.RulesFactory;
import be.alexandre01.dreamnetwork.api.events.EventCatcher;
import be.alexandre01.dreamnetwork.api.events.Listener;
import be.alexandre01.dreamnetwork.api.events.list.commands.CoreCommandExecuteEvent;
import be.alexandre01.dreamnetwork.api.events.list.services.CoreServiceLinkedEvent;
import be.alexandre01.dreamnetwork.api.events.list.services.CoreServiceStopEvent;
import be.alexandre01.dreamnetwork.api.service.IService;

/*
 ↬   Made by Alexandre01Dev 😎
 ↬   done on 05/10/2023 at 12:33
*/
public class ProxyLifecycleListener implements Listener {
    MainAddon mainAddon;
    public ProxyLifecycleListener(MainAddon mainAddon){
        this.mainAddon = mainAddon;
    }


    @EventCatcher
    public void onStart(CoreServiceLinkedEvent event){
        IService service = event.getService();
        if(service.getJvmExecutor().isProxy()){
            String port = String.valueOf(service.getJvmExecutor().getPort());
            mainAddon.getRulesFactory().addRule(port,new RulesFactory.Rule(port));
        }
    }

    @EventCatcher
    public void onStop(CoreServiceStopEvent event){
        IService service = event.getService();
        if(service.getJvmExecutor().isProxy()){
            String port = String.valueOf(service.getJvmExecutor().getPort());
            mainAddon.getRulesFactory().removeRule(port);
        }
    }
}

