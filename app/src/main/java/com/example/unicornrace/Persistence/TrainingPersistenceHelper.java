package com.example.unicornrace.Persistence;

import android.content.Context;

import com.example.unicornrace.Models.Training;

import java.util.List;

public class TrainingPersistenceHelper {
    public static final String LOG_CLASS = TrainingPersistenceHelper.class.getName();


    public static List<Training> getAllItems(Context context) {
        return new TrainingDbHelper(context).getAllTrainings();
    }


    public static Training getItem(long id, Context context) {
        return new TrainingDbHelper(context).getTraining((int) id);
    }


    public static Training getActiveItem(Context context) {
        return new TrainingDbHelper(context).getActiveTraining();
    }

    /**
     * Stores the given training session to database.
     * If id is set, the training session will be updated else it will be created
     *
     * @param item    the training session to store
     * @param context The application context
     * @return the saved training session (with correct id)
     */
    public static Training save(Training item, Context context) {
        if (item == null) {
            return null;
        }
        if (item.getId() <= 0) {
            long insertedId = insert(item, context);
            if (insertedId == -1) {
                return null;
            } else {
                item.setId(insertedId);
                return item;
            }
        } else {
            int affectedRows = update(item, context);
            if (affectedRows == 0) {
                return null;
            } else {
                return item;
            }
        }
    }


    public static boolean delete(Training item, Context context) {
        new TrainingDbHelper(context).deleteTraining(item);
        return true;
    }


    protected static long insert(Training item, Context context) {
        return new TrainingDbHelper(context).addTraining(item);
    }

    protected static int update(Training item, Context context) {
        return new TrainingDbHelper(context).updateTraining(item);
    }
}
