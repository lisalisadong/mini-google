package crawler.robots;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PrimaryKey;

@Persistent
public class RobotPath {
	
	public LinkedList<String> path;
	public HashMap<String, String> params;
	public HashMap<String, String> starParams;
	
	public boolean hasParam;
	
	public RobotPath() { }
	
	public RobotPath(String host, String link) {
		
		path = new LinkedList<>();
		params = new HashMap<>();
		starParams = new HashMap<>();
		
		String[] strs = link.split("\\?");
		
		if(strs.length == 0) return; 
		String rawPath = strs[0];
		int l = rawPath.length();
		if(l > 0 && rawPath.charAt(0) != '/') rawPath = "/" + rawPath;
//		if(l > 0 && rawPath.charAt(l - 1) != '/') rawPath = rawPath + '/';
		int i = 0;
		while(true) {
			int idx = rawPath.indexOf('/', i + 1);
			if(idx == -1) break;
			
			path.offer(rawPath.substring(i, idx));
			i = idx;
		}
		path.offer(rawPath.substring(i));
//		System.out.println(toString());
		hasParam = link.contains("?");
		if(strs.length == 1) return;
		
		// store params
		String[] pairs = strs[1].split("&");
		for(String pair: pairs) {
			String[] kv = pair.split("=");
			if(kv.length != 2) continue;
			
			// TODO:
			// not take care of *
			if(kv[0].contains("*")) starParams.put(kv[0], kv[1]);
			else params.put(kv[0], kv[1]);
			
		}
		
	}
	
	public String toString() {
		return path.toString();
	}
	
	public boolean matchPath(RobotPath toMatch) {
		Iterator<String> p = path.iterator();
		Iterator<String> tm = toMatch.path.iterator();
		while(p.hasNext() && tm.hasNext()) {
			String s1 = p.next();
			String s2 = tm.next();
//			System.out.println("Compare " + s1 + " and " + s2);
			// last one
			if(!p.hasNext()) {
				
				if(s1.contains("*")) {
					
					int idx = s1.indexOf('*');
					String p1 = s1.substring(0, idx);
					if(!s2.startsWith(p1)) return false;
					
					if(idx == s1.length() - 1) return true;
					String p2 = s1.substring(idx + 1);
					if(!s1.endsWith("$") && s2.contains(p2)) return true;
					
					while(tm.hasNext()) {
						s2 = tm.next();
						if(!s1.endsWith("$") && s2.contains(p2)) return true;
					}
					
//					System.out.println(toMatch.hasParam);
					return s1.endsWith("$") && !toMatch.hasParam
							&& s2.endsWith(p2.substring(0, p2.length() - 1));
					
				} else {
					// s1 no *
					if(s1.endsWith("$")) {
						return s2.equals(s1.substring(0, s1.length() - 1));
					} 
					return s2.startsWith(s1);
					
				}
				
			}
			
			// in the middle of path
			if(s1.contains("*")) {
				// TODO:
				int idx = s1.indexOf('*');
				String p1 = s1.substring(0, idx);
				if(!s2.startsWith(p1)) return false;
				if(idx == s1.length() - 1) continue;
				String p2 = s1.substring(idx + 1);
				if(!s2.endsWith(p2)) return false;
			} else {
				if(!s1.equals(s2)) return false;
			}
			
			
		}
		
		return !p.hasNext();
	}
	
	public boolean matchParams(RobotPath toMatch) {
		for(String k: params.keySet()) {
			if(!matchString(params.get(k), toMatch.params.get(k))) return false;
		}
		
		for(String k: starParams.keySet()) {
			boolean flag = false;
			for(String kk: toMatch.params.keySet()) {
				if(matchString(k, kk) 
						&& matchString(starParams.get(k), toMatch.params.get(kk)))
				{
					flag = true;
					break;
				}
			}
			if(!flag) return false;
		}
		
		return true;
	}
	
	public boolean matchString(String t, String s) {
		
//		System.out.println("Match " + t + ", " + s);
		if(s == null) return false;
		int idx = t.indexOf("*");
		if(idx == -1) return t.equals(s);
		
		String p1 = t.substring(0, idx);
		if(!s.startsWith(p1)) return false;
		if(idx == t.length() - 1) return true;
		
		String p2 = t.substring(idx + 1);
		return s.endsWith(p2);
	}
	
	public boolean match(RobotPath toMatch) {
//		System.out.println("Match: " + toString() + " and " + toMatch.toString());
		return matchPath(toMatch) && matchParams(toMatch);
	}
	
	public static void main(String[] args) {
		
//		RobotPath p = new RobotPath("/r/*/comments/*/*/c*");
//		
//		System.out.println("Match: ");
//		System.out.println(p.match(new RobotPath("/r/sadasadas/comments/a/c/cia")));
//		System.out.println(p.matchPath(new RobotPath("/fish")));
//		System.out.println(p.matchPath(new RobotPath("/fish.html")));
//		System.out.println(p.matchPath(new RobotPath("/fish/salmon.html")));
//		System.out.println(p.matchPath(new RobotPath("/fishheads")));
//		System.out.println(p.matchPath(new RobotPath("/fishheads/yummy.html")));
//		System.out.println(p.matchPath(new RobotPath("/fish.php?id=anything")));
//		
//		System.out.println("Not match: ");
//		System.out.println(p.matchPath(new RobotPath("/Fish.asp")));
//		System.out.println(p.matchPath(new RobotPath("/catfish")));
//		System.out.println(p.matchPath(new RobotPath("/?id=fish")));
//		System.out.println(p.matchPath(new RobotPath("/windows.PHP")));
//		System.out.println("/help/maps/indoormaps/partners?".contains("?"));
	}
	
}
