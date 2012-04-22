/*
Copyright 2012 Portland Transport

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.transitappliance.loader;

import java.util.List;

public class TransitStopLoader {
    // Spring-configured stuff

    private String agencyId;
    public void setAgencyId (String agencyId) {
        this.agencyId = agencyId;
    }

    private String rightsNotice;
    public void setRightsNotice (String rightsNotice) {
        this.rightsNotice = rightsNotice;
    }

    private TransitDataSource dataSource;
    public void setDataSource (TransitDataSource dataSource) {
        this.dataSource = dataSource;
    }

    // This will actually be a subclass of StopSaver 
    private StopSaver stopSaver;
    
    /**
     * Set the StopSaver to use. Most will use CouchDB.
     * @param stopSaver the StopSaver to use
     */
    public void setStopSaver(StopSaver stopSaver) {
        this.stopSaver = stopSaver;
    }

    private List<StopModifier> stopModifiers;
    /**
     * StopModifiers that should be applied before saving
     */
    public void setStopModifiers (List<StopModifier> stopModifiers) {
        this.stopModifiers = stopModifiers;
    }

    public void loadStops () {
        System.out.println("Running load for agency " + agencyId);

        int stopCount = 0;
        TAAgency agency;

        dataSource.initialize(agencyId);

        stopSaver.initialize();

        TAStop currentStop;

        // main loop
        while (true) {
            currentStop = dataSource.getStop();
            
            // a null here indicates there are no more stops to load
            if (currentStop == null)
                break;

            // in case we delete it, keep this arounf
            String stopId = currentStop.id;

            // Process the stop through the modifiers
            for (StopModifier modifier : stopModifiers) {
                currentStop = modifier.processStop(currentStop);

                // a null here indicates this stop should be removed
                // no point in continuing
                if (currentStop == null) {
                    System.out.println("Removing stop " + stopId + " by modifier");
                    break;
                }
            }

            // null from above
            if (currentStop == null)
                continue;

            stopSaver.saveStop(currentStop);
        }

        // save the agency
        agency = dataSource.getAgency();
        // this is set here to avoid duplication
        agency.rights_notice = rightsNotice;
        stopSaver.saveAgency(agency);
            
        stopSaver.serialize();       
    }
}