package server.utility;

import common.entity.Worker;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import server.dbmanagers.DBManager;

/** Клаас для Управления коллекцией. */
public class CollectionManager {
    private int currentId = 1;
    private static final Logger COLLECTIONLOGGER =
            Logger.getLogger("server.utility.CollectionHandler");
    private Map<Integer, Worker> workers = new HashMap<>();
    private TreeSet<Worker> collection = new TreeSet<>();
    private LocalDateTime lastInitTime;
    private LocalDateTime lastSaveTime;

    public CollectionManager() {
        try {
            this.setCollection(new DBManager().readAllWorkers());
            this.currentId = collection.last().getId();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return Последнее время инициализации.
     */
    public LocalDateTime getLastInitTime() {
        return lastInitTime;
    }

    /**
     * @return Последнее время сохранения.
     */
    public LocalDateTime getLastSaveTime() {
        return lastSaveTime;
    }

    /**
     * @return коллекция.
     */
    public TreeSet<Worker> getCollection() {
        return collection;
    }

    public void setCollection(Vector<Worker> collection) {
        this.collection = new TreeSet<>(collection);
        this.workers.putAll(collection.stream().collect(Collectors.toMap(Worker::getId, w -> w)));
    }

    /**
     * @return коллекция.
     */
    public ArrayList<Worker> getSortCollection() {
        ArrayList<Worker> newCollection = new ArrayList<>(collection);
        newCollection.sort(
                new Comparator<Worker>() {
                    public int compare(Worker s1, Worker s2) {
                        return s1.getSalary() - s2.getSalary();
                    }
                });
        return newCollection;
    }

    /** Получить Worker по ID */
    public Worker byId(int id) {
        return workers.get(id);
    }

    /** Содержит ли колекции Worker */
    public boolean isContain(Worker e) {
        return e == null || byId(e.getId()) != null;
    }

    /** Получить свободный ID */
    public synchronized int getFreeId() {
        while (byId(++currentId) != null) if (++currentId < 0) currentId = 1;
        return currentId;
    }

    /** Добавляет Worker */
    public synchronized boolean add(Worker a) {
        if (isContain(a)) return false;
        if (new DBManager().write(a)) {
            a.setId(getFreeId());
            workers.put(a.getId(), a);
            collection.add(a);
            COLLECTIONLOGGER.fine("В коллекцию добавлен новый рабочий.");
            return true;
        }
        return false;
    }

    /** Обновляет Worker */
    public synchronized boolean update(Worker a) {
        if (!isContain(a)) return false;
        if (new DBManager().update(a)) {
            collection.remove(byId(a.getId()));
            workers.put(a.getId(), a);
            collection.add(a);
            return true;
        }
        return false;
    }

    /** Удаляет Worker по ID */
    public synchronized boolean remove(int id) {
        var a = byId(id);
        if (a == null) return false;
        if (new DBManager().remove(id)) {
            workers.remove(a.getId());
            collection.remove(a);
            return true;
        }
        return false;
    }

    /** очищает коллекцию */
    public void clear() {
        collection.clear();
        workers.clear();
    }

    @Override
    public String toString() {
        if (collection.isEmpty()) return "Коллекция пуста!";

        StringBuilder info = new StringBuilder();
        for (var Worker : collection) {
            info.append(Worker + "\n\n");
        }
        return info.toString().trim();
    }
}
