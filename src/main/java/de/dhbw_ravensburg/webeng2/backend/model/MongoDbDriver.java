package de.dhbw_ravensburg.webeng2.backend;

import static com.mongodb.client.model.Filters.eq;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoDbDriver {
    public static boolean connected = false;
    private static MongoClient mongoClient; 

    public static boolean connect() {

        // Replace the placeholder with your MongoDB deployment's connection string
        String uri = "mongodb://root:example@localhost:27017";

        try {
            mongoClient = MongoClients.create(uri);
            connected = true;
        }
        catch (Exception e){
            System.out.println("Could not connect to MongdoDB:");
            //System.out.println(e);
            connected = false;
        }

        return connected;
    }
}