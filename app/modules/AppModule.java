package modules;

import com.google.inject.AbstractModule;
import models.services.SearchService;
import models.services.SearchServiceImpl;

public class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(SearchService.class).to(SearchServiceImpl.class);
    }
}