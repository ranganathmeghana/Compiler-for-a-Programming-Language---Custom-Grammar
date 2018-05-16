package cop5556sp17;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import cop5556sp17.AST.Dec;

public class SymbolTable {

	Stack<Integer> scope_stack;
	Map<String, List<StackObject>> map;

	class StackObject {

		private int scope;

		public int getScope() {
			return scope;
		}

		public void setScope(int scope) {
			this.scope = scope;
		}

		public Dec getDec() {
			return dec;
		}

		public void setDec(Dec dec) {
			this.dec = dec;
		}

		private Dec dec;

		public StackObject(int scope, Dec dec) {
			// TODO Auto-generated constructor stub
			this.scope = scope;
			this.dec = dec;
		}

	}

	// TODO add fields
	int current_scope = 0;
	int next_scope = 1;

	/**
	 * to be called when block entered
	 */
	public void enterScope() {
		// TODO: IMPLEMENT THIS
		current_scope = next_scope++;
		scope_stack.push(current_scope);

	}

	/**
	 * leaves scope
	 */
	public void leaveScope() {
		// TODO: IMPLEMENT THIS
		current_scope = scope_stack.pop();
	}

	public boolean insert(String ident, Dec dec) {

		List<StackObject> object;

		if (map.containsKey(ident)) {
			object = map.get(ident);

			for (StackObject object2 : object) {
				if ((object2.scope) == current_scope) {
					return false;
				}
			}

			object.add(new StackObject(current_scope, dec));

		} else {

			StackObject stackObject = new StackObject(current_scope, dec);
			object = new ArrayList<StackObject>();
			object.add(stackObject);
			map.put(ident, object);
		}

		// TODO: IMPLEMENT THIS
		return true;
	}

	public Dec lookup(String ident) {
		// TODO: IMPLEMENT THIS
		List<StackObject> object;

		if (map.containsKey(ident)) {
			object = map.get(ident);

			for (int top_scope = scope_stack.size() - 1; top_scope >= 0; top_scope--) {
				for (StackObject object3 : object) {
					if (object3.getScope() == scope_stack.get(top_scope)) {
						return object3.getDec();
					}
					//return null;
				}
			}
		}
		return null;
	}

	public SymbolTable() {
		// TODO: IMPLEMENT THIS
		scope_stack = new Stack<Integer>();
		map = new HashMap<String, List<StackObject>>();
		scope_stack.push(0);

	}

	@Override
	public String toString() {
		//TODO:  IMPLEMENT THIS
		
		StringBuilder output=new StringBuilder();
		output.append("current scope:" +current_scope);
		return "";
	}

}
