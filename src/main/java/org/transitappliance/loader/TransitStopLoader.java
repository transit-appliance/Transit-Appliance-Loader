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

public class TransitStopLoader {
    // Spring-configured stuff

    private String agencyId;
    public void setAgencyId (String agencyId) {
        this.agencyId = agencyId;
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

    public void loadStops () {
        System.out.println("Running load for agency " + agencyId);

        int stopCount = 0;

        dataSource.initialize(agencyId);

        stopSaver.initialize();

        TAStop currentStop;
        
        // main loop
        while (true) {
            currentStop = dataSource.getStop();
            
            if (currentStop == null)
                break;

            stopSaver.saveStop(currentStop);
        }
            
        stopSaver.serialize();       
    }
}