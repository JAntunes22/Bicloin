package pt.tecnico.rec;

import java.util.HashMap;
import java.util.ArrayList;

public class Record {
    
    HashMap<String, ArrayList<Integer>> _records = new HashMap<String, ArrayList<Integer>>();

    public synchronized void writeRecord(String id, int value, int tag, int cid) {

		if (_records.containsKey(id)) {
			if (tag < _records.get(id).get(1) || (tag == _records.get(id).get(1) && cid == _records.get(id).get(2))) {
				 return;
			}
		}

		ArrayList<Integer> values = new ArrayList<Integer>();
		values.add(value);
		values.add(tag);
		values.add(cid);
		_records.put(id, values);
	}
	
	public synchronized ArrayList<Integer> readRecord(String id) throws NullPointerException {
		return _records.get(id);
	}

	public synchronized void removeRecord(String id) {
		_records.remove(id);
	}

	public synchronized void clearRecords() {
		_records.clear();
	}
}
