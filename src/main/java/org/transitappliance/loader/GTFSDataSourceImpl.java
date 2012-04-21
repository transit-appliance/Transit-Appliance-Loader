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
    // This is used to store the TAStops
    private TAStop[] stops;

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
    public void initialize (String agencyId) {
        super.initialize(agencyId);
        System.out.println("Reading GTFS from " + filePath);
        readAndProcessGtfs();
    }

    public TAStop getStop () {
        stopIndex++;
        if (stopIndex < stops.length)
            return stops[stopIndex];

        else
            return null;
    }

    /** 
     * Read the GTFS and process it to what we need for TA.
     */
    private void readAndProcessGtfs () {
        GtfsReader reader = new GtfsReader();

        // This builds stop <-> route mappings. Save a reference so we can pull it out later
        NoStopTimeDaoImpl store = new NoStopTimeDaoImpl();

        reader.setEntityStore(store);
        reader.setDefaultAgencyId(agencyId);
        
        try {
            reader.setInputLocation(new File(filePath));
            reader.run();
        } 
        catch (IOException e) {
            System.out.println("Error reading GTFS " + filePath);
            System.exit(1);
        }

        stops = store.getStops();
    }
}