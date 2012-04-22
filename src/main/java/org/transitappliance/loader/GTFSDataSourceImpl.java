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

    private TAAgency agency;

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
     * If the GTFS has multiple agencies, this must be set so that the correct one is used (Loader does not
     * support multiagency GTFS).
     */
    private String gtfsAgencyId;
    public void setGtfsAgencyId (String gtfsAgencyId) {
        this.gtfsAgencyId = gtfsAgencyId;
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

    public TAAgency getAgency () {
        return agency;
    }

    /** 
     * Read the GTFS and process it to what we need for TA.
     */
    private void readAndProcessGtfs () {
        GtfsReader reader = new GtfsReader();
        Agency agencyIn;

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

        // build the agency
        // if this is multiagency gtfs and none is selected
        if (gtfsAgencyId == null && store.getAllAgencies().size() > 1) {
            System.out.println("GTFS has multiple agencies and gtfsAgencyId is not defined. Set it to one of:");
            for (Agency agency : store.getAllAgencies()) {
                System.out.println(agency.getId() + "(" + agency.getName() + ")");
            }
            System.exit(1);
        }

        if (gtfsAgencyId == null) {
            agencyIn = (Agency) store.getAllAgencies().toArray()[0];
        }
        else {
            agencyIn = store.getAgencyForId(gtfsAgencyId);
        }

        // now build the agency
        agency = new TAAgency();
        // use the one in the config file not from GTFS
        agency.id = agencyId;
        agency.agency_lang = agencyIn.getLang();
        agency.agency_name = agencyIn.getName();
        agency.agency_timezone = agencyIn.getTimezone();
        agency.agency_url = agencyIn.getUrl();
        // agency.rights_notice set in TransitStopLoader to avoid duplication
    }
}