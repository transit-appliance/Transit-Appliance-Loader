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

import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;

public class GTFSDataSourceImpl extends TransitDataSource {
    // All the stops, routes and stop<->route mappings are saved up front, then the routes are 
    // merged into the stops
    private HashMap<AgencyAndId, TAStop> stops = new HashMap<AgencyAndId, TAStop>();
    // route ids, routes
    private HashMap<AgencyAndId, TARoute> routes = new HashMap<AgencyAndId, TARoute>();
    // stop IDs, route IDs
    private HashMap<AgencyAndId, ArrayList<AgencyAndId>> stopRouteMapping = new HashMap<AgencyAndId, ArrayList<AgencyAndId>>();
    // this stores the stops in an unordered fashion when returning them
    private ArrayList<TAStop> stopsFinal = new ArrayList<TAStop>();

    // we want to start with the first stop, and this is incremented at the start of the loop
    private int stopIndex = -1;

    // This is a string, not a file, b/c eventually we'll want to download http://, ftp:// urls
    // TODO: downloads
    private String filePath;

    /**
     * The path to the GTFS file. For now it needs to be a local file.
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Initialize the GTFS source: retrieve and unzip.
     */
    @Override
    public void initialize () {
        System.out.println("Reading GTFS from " + filePath);
        readAndProcessGtfs();
    }


    /**
     * This saves the relevant entities and discards the rest
     */
    private class TAEntityHandler implements EntityHandler {
        private int stopCount = 0;
        private int routeCount = 0;
        private long stopTimeCount = 0;

        /**
         * If stop or route, store. If StopTime, parse mapping. If agency, store. Otherwise, discard.
         */
        @Override
        public void handleEntity(Object entity) {
            if (entity instanceof Stop) {
                Stop stop = (Stop) entity;
                
                TAStop dbStop = new TAStop();

                AgencyAndId stopId = stop.getId();
                
                dbStop.agency = stopId.getAgencyId();
                dbStop.stop_id = stopId.getId();
                dbStop.id = dbStop.agency + ":" + dbStop.stop_id;
                dbStop.stop_lat = stop.getLat();
                dbStop.stop_lon = stop.getLon();
                dbStop.stop_desc = stop.getDesc();
                dbStop.stop_name = stop.getName();
                dbStop.stop_code = stop.getCode();
                // TODO: extra attributes in attributes HashMap

                stops.put(stopId, dbStop);

                stopCount++;
                if (stopCount % 500 == 0)
                    System.out.println("Stops: " + stopCount);
            }

            else if (entity instanceof Route) {
                Route route = (Route) entity;
                
                TARoute dbRoute = new TARoute();
                
                AgencyAndId aid = route.getId();

                dbRoute.route_id = aid.getId();
                dbRoute.route_url = route.getUrl();
                dbRoute.route_long_name = route.getLongName();
                dbRoute.route_short_name = route.getShortName();
                dbRoute.route_type = route.getType();
                
                routes.put(aid, dbRoute);

                routeCount++;
                if (routeCount % 25 == 0)
                    System.out.println("Routes: " + routeCount);
            }

            else if (entity instanceof StopTime) {
                StopTime st = (StopTime) entity;
                
                // parse it down to a mapping
                AgencyAndId stopId = st.getStop().getId();
                AgencyAndId routeId = st.getTrip().getRoute().getId();

                if (!stopRouteMapping.containsKey(stopId)) {
                    stopRouteMapping.put(stopId, new ArrayList<AgencyAndId>());
                }
                
                ArrayList routesForStop = stopRouteMapping.get(stopId);
                if (!routesForStop.contains(routeId)) {
                    routesForStop.add(routeId);
                    stopRouteMapping.put(stopId, routesForStop);
                }

                stopTimeCount++;
                if (stopTimeCount % 100000 == 0) {
                    System.out.println("Stop times: " + stopTimeCount);
                }
            }

            // TODO: agencies
        }
    }

    public TAStop getStop () {
        stopIndex++;
        return stopsFinal.get(stopIndex);
    }

    /** 
     * Read the GTFS and process it to what we need for TA.
     */
    private void readAndProcessGtfs () {
        GtfsReader reader = new GtfsReader();

        reader.addEntityHandler(new TAEntityHandler());
        
        try {
            reader.setInputLocation(new File(filePath));
            reader.run();
        } 
        catch (IOException e) {
            System.out.println("Error reading GTFS " + filePath);
            System.exit(1);
        }

        // now, process the relations
        for (AgencyAndId stopId : stops.keySet()) {
            TAStop stop = stops.get(stopId);
           
            ArrayList<AgencyAndId> routesForStop = stopRouteMapping.get(stopId);
            
            if (routesForStop == null) {
                System.out.println("Warning: stop " + stopId + " has no routes; removing");
                stops.remove(stopId);
                continue;
            }

            // there are no duplicates to worry about; that is checked above
            for (AgencyAndId routeId : routesForStop) {
                stop.routes.add(routes.get(routeId));
            }

            // add it to the output list and remove it from the input to save ram
            stopsFinal.add(stop);
            stops.remove(stopId);

            //System.out.println("Stop " + stopId + " has " + stop.routes.size() + " routes");
        }

        System.out.println("GTFS read; stops " + stopsFinal.size() + ", routes " + routes.size());                          
    }
}