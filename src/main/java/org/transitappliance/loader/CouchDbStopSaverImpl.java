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
import java.util.List;
import java.io.IOException;
import com.fourspaces.couchdb.Session;
import com.fourspaces.couchdb.Database;
import com.fourspaces.couchdb.Document;
import com.fourspaces.couchdb.ViewResults;

public class CouchDbStopSaverImpl implements StopSaver {
    private Session session;
    private Database db;
    private ArrayList<Document> stops = new ArrayList<Document>();
    
    // spring
    private String dbHost;
    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    private int dbPort;
    public void setDbPort(int dbPort) {
        this.dbPort = dbPort;
    }

    private String dbName;
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
   
    /** use the bulk update command to upload documents? */
    private boolean bulkUpdate = true;
    public void setBulkUpdate(boolean bulkUpdate) {
        this.bulkUpdate = bulkUpdate;
    }

    public void initialize () {
        session = new Session(dbHost, dbPort);

        // get or create database
        if (session.getDatabaseNames().contains(dbName)) {
            System.out.println("Warning: DB " + dbName + " already exists on " + dbHost + ":" + dbPort);
            db = session.getDatabase(dbName);
        }
        else {
            db = session.createDatabase(dbName);
        }
    }

    public void saveStop (TAStop stop) {
        Document doc = new Document();

        doc.put("_id", stop.id);
        doc.put("agency", stop.agency);
        doc.put("stop_lat", stop.stop_lat);
        doc.put("stop_lon", stop.stop_lon);
        doc.put("stop_desc", stop.stop_desc);
        doc.put("stop_id", stop.stop_id);
        doc.put("stop_name", stop.stop_name);
        doc.put("stop_code", stop.stop_code);
        doc.put("avl_stop_id", stop.avl_stop_id);

        doc.put("routes", makeRouteDocs(stop.routes));

        // geocouch
        Document geo = new Document();

        // in X, Y for GeoCouch
        double[] coords = new double[2];
        coords[0] = stop.stop_lon;
        coords[1] = stop.stop_lat;

        geo.put("type", "Point");
        geo.put("coordinates", coords);

        doc.put("geometry", geo);

        stops.add(doc);
    }

    /**
     * Make serializable routes for saving in Couch.
     * @param routes The routes to serialize
     */
    private Document[] makeRouteDocs(List<TARoute> routes) {
        ArrayList<Document> routesOut = new ArrayList<Document>();
        Document routeOut;
        Document[] template = new Document[1];
        
        // Make each route into a Document
        for (TARoute route : routes) {
            routeOut = new Document();

            routeOut.put("route_id", route.route_id);
            routeOut.put("route_url", route.route_url);
            routeOut.put("route_long_name", route.route_long_name);
            routeOut.put("route_type", route.route_type);
            routeOut.put("route_short_name", route.route_short_name);
            routeOut.put("avl_route_id", route.avl_route_id);

            routesOut.add(routeOut);
        }

        return routesOut.toArray(template);
    }

    /**
     * Save the agency from a TAAgency object
     * @param agency The agency to save.
     */
    public void saveAgency (TAAgency agency) {
        Document doc = new Document();
        doc.put("_id", agency.id);
        doc.put("agency_lang", agency.agency_lang);
        doc.put("agency_name", agency.agency_name);
        doc.put("agency_timezone", agency.agency_timezone);
        doc.put("agency_url", agency.agency_url);
        doc.put("avl_agency_id", agency.avl_agency_id);
        doc.put("avl_service", agency.avl_service);
        doc.put("rights_notice", agency.rights_notice);

        // it's not really a stop, but it should be serialized as well
        stops.add(doc);
    }

    public void serialize () {
        Document[] template = new Document[1];

        // delete all existing stops
        ViewResults allDocs = db.getAllDocuments();
        for (Document d : allDocs.getResults()) {
            try {
                db.deleteDocument(d);
            } catch (IOException e) {
                System.out.println("Error deleting " + d.getId() +". Unpredictable things may occur.");
            }
        }

        try {
            db.bulkSaveDocuments(stops.toArray(template));
        } catch (IOException e) {
            System.out.println("Error saving stops");
            System.exit(1);
        }

        System.out.println("Stops saved");

    }
}
        
        