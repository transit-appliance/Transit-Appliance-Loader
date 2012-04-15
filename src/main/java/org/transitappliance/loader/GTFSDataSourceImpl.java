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
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.csv_entities.EntityHandler;
import org.onebusawaygtfs.model.Stop;
import org.onebusawaygtfs.model.Route;
import org.onebusawaygtfs.model.StopTime;
import org.onebusawaygtfs.model.Agency;
import org.onebusawaygtfs.model.AgencyAndId;

public class GTFSDataSourceImpl extends TransitDataSource {
    // All the stops, routes and stop<->route mappings are saved up front, then the routes are merged into the stops
    private ArrayList<TAStop> stops = new ArrayList<TAStop>();
    // route ids, routes
    private HashMap<AgencyAndId, TARoute> routes = new HashMap<String, TARoute>();
    // stop IDs, route IDs
    private HashMap<AgencyAndId, ArrayList<AgencyAndId>> stopRouteMapping = new HashMap<AgencyAndId, ArrayList<AgencyAndId>>();
    private int stopIndex = 0;

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
        readAndProcessGtfs();
    }


    /**
     * This saves the relevant entities and discards the rest
     */
    private class TAEntityHandler extends EntityHandler {
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

                // Stop.getId() returns AgencyAndId
                dbStop.agency = stop.getId().getAgencyId();
                dbStop.stop_id = stop.getId().getId();
                dbStop.id = dbStop.agency + ":" + dbStop.stop_id;
                dbStop.stop_lat = stop.getLat();
                dbStop.stop_lon = stop.getLon();
                dbStop.stop_desc = stop.getDesc();
                dbStop.stop_name = stop.getName();
                dbStop.stop_code = stop.getCode();
                // TODO: extra attributes in attributes HashMap

                stops.add(dbStop);

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
                AgencyAndId routeId = st.getRoute().getId();

                if (!routes.containsKey(stopId)) {
                    routes.put(stopId, new ArrayList<AgencyAndId>());
                }
                
                ArrayList routesForStop = routes.get(stopId);
                if (!routesForStop.contains(routeId)) {
                    routesForStop.add(routeId);
                    routes.put(stopId, routesForStop);
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
        return null;
    }

    /**
     * An entity handler that converts GTFS to TA types, saves route mappings and discards everythign else.
     */

    /** 
     * Read the GTFS and process it to what we need for TA.
     */
    private void readAndProcessGtfs () {
        GtfsReader reader = new GtfsReader();
        reader.setInputLocation(filePath);
        
        reader.addEntityHandler(new TAEntityHandler());

        reader.run();
    }
}