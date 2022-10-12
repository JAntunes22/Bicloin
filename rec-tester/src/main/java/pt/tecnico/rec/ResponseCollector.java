package pt.tecnico.rec;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

public class ResponseCollector<R> {

    private volatile float _quorum;
    private volatile int _total;
    private volatile float _count;
    
    private CopyOnWriteArrayList<R> _responses = new CopyOnWriteArrayList<R>();
    private ConcurrentHashMap<String, R> _responsesPath = new ConcurrentHashMap<String, R>();
    private CopyOnWriteArrayList<Throwable> _errors = new CopyOnWriteArrayList<Throwable>();
    
    public ResponseCollector(float quorum, int total) {
        _quorum = quorum;
        _total = total;
    }

    public synchronized void addResponse(R r, float weight) {
        _responses.add(r);
        _count += weight;
        _total--;

        if (verify()) {
            notify();
        }
    }

    // if it receives a path, add response to a hashmap instead of a list, to associate each response to its server
    public synchronized void addResponse(R r, float weight, String path) {
        _responsesPath.put(path, r);
        _count += weight;
        _total--;

        if (verify()) {
            notify();
        }
    }

    public CopyOnWriteArrayList<R> getResponses() {
        return _responses;
    }

    public ConcurrentHashMap<String, R> getResponsesPath() {
        return _responsesPath;
    }

    public int getCount() {
        return _responses.size();
    } 

    public CopyOnWriteArrayList<Throwable> getErrors() {
        return _errors;
    }

    // verify if it already has enough responses for the quorum, or if there aren't any responses left
    public boolean verify() {
        if (_quorum <= _count) return true;
        return _total == 0;
    }

    public int size() {
        return _responses.size();
    }

	public synchronized void addError(Throwable throwable) {
        _total--;
        _errors.add(throwable);

        if (verify()) {
            notify();
        }
	}

}
