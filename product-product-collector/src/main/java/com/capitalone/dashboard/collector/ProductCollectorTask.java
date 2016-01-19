package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.Dashboard;
import com.capitalone.dashboard.model.ProductDashboardCollector;
import com.capitalone.dashboard.model.TeamDashboardCollectorItem;
import com.capitalone.dashboard.repository.BaseCollectorRepository;
import com.capitalone.dashboard.repository.ProductDashboardRepository;
import com.capitalone.dashboard.repository.TeamDashboardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ProductCollectorTask extends CollectorTask<ProductDashboardCollector> {

    private final ProductDashboardRepository productDashboardRepository;
    private final TeamDashboardRepository teamDashboardRepository;
    private final ProductClient productClient;
    private final ProductSettings productSettings;

    @Autowired
    public ProductCollectorTask(TaskScheduler taskScheduler,
                                ProductDashboardRepository productDashboardRepository,
                                TeamDashboardRepository teamDashboardRepository,
                                ProductClient productClient,
                                ProductSettings productSettings) {
        super(taskScheduler, "Product");
        this.productDashboardRepository = productDashboardRepository;
        this.teamDashboardRepository = teamDashboardRepository;
        this.productClient = productClient;
        this.productSettings = productSettings;
    }

    @Override
    public String getCron() {
        return productSettings.getCron();
    }

    @Override
    public ProductDashboardCollector getCollector() {
        return ProductDashboardCollector.prototype();
    }

    @Override
    public BaseCollectorRepository<ProductDashboardCollector> getCollectorRepository() {
        return productDashboardRepository;
    }

    @Override
    public void collect(ProductDashboardCollector collector) {
        //find all team dashboards collector items that exist
        List<TeamDashboardCollectorItem> existingTeamDashboardCollectorItems = teamDashboardRepository.findTeamDashboards(collector.getId());
        Map<TeamDashboardCollectorItem, Dashboard> createdTeamDashboards = productClient.getTeamDashboards();
        List<TeamDashboardCollectorItem> toUpdate = new ArrayList<>();

        //find all collector items that are new
        for ( Map.Entry entry : createdTeamDashboards.entrySet()) {
            //if collectoritem for the dashboard in the repository doesnt exist
            // as a collector item add it to the list of items to save
            if(!existingTeamDashboardCollectorItems.contains(entry.getKey())) {
                TeamDashboardCollectorItem collectorItem = ((TeamDashboardCollectorItem)entry.getKey());
                collectorItem.setCollectorId(collector.getId());
                collectorItem.setDescription(((Dashboard)entry.getValue()).getTitle());
                toUpdate.add(collectorItem);
            }
        }

        //then, save our list of new entries
        teamDashboardRepository.save(toUpdate);

        //TODO: collect data
    }
}
