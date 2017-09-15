package com.example.morenkov.config;

import com.example.morenkov.rest.AccountRestService;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.wadl.internal.WadlResource;
import org.springframework.stereotype.Component;

@Component
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        // Register endpoints, providers, ...
        this.registerEndpoints();
    }

    private void registerEndpoints() {
        this.register(AccountRestService.class);
        // Access through /<Jersey's servlet path>/application.wadl
        this.register(WadlResource.class);
        this.register(OptionsAcceptPatchHeaderFilter.class);
        this.register(PatchingInterceptor.class);
    }
}
