import java.util.*;

public class SuffixAutomaton {

	public static class State {
		int length;
		int suffLink;
		List<Integer> invSuffLinks = new ArrayList<>(0);
		int firstPos = -1;
		int[] next = new int[128];

		{
			Arrays.fill(next, -1);
		}
	}

	public static State[] buildSuffixAutomaton(CharSequence s) {
		int n = s.length();
		State[] st = new State[Math.max(2, 2 * n - 1)];
		st[0] = new State();
		st[0].suffLink = -1;
		int last = 0;
		int size = 1;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			int cur = size++;
			st[cur] = new State();
			st[cur].length = i + 1;
			st[cur].firstPos = i;
			int p = last;
			for (; p != -1 && st[p].next[c] == -1; p = st[p].suffLink) {
				st[p].next[c] = cur;
			}
			if (p == -1) {
				st[cur].suffLink = 0;
			} else {
				int q = st[p].next[c];
				if (st[p].length + 1 == st[q].length) {
					st[cur].suffLink = q;
				} else {
					int clone = size++;
					st[clone] = new State();
					st[clone].length = st[p].length + 1;
					System.arraycopy(st[q].next, 0, st[clone].next, 0, st[q].next.length);
					st[clone].suffLink = st[q].suffLink;
					for (; p != -1 && st[p].next[c] == q; p = st[p].suffLink) {
						st[p].next[c] = clone;
					}
					st[q].suffLink = clone;
					st[cur].suffLink = clone;
				}
			}
			last = cur;
		}
		for (int i = 1; i < size; i++) {
			st[st[i].suffLink].invSuffLinks.add(i);
		}
		return Arrays.copyOf(st, size);
	}

	// random tests
	public static void main(String[] args) {
		Random rnd = new Random(1);
		for (int step = 0; step < 100_000; step++) {
			int len1 = rnd.nextInt(20);
			int len2 = rnd.nextInt(20) + 1;
			String a = getRandomString(len1, rnd);
			String b = getRandomString(len2, rnd);
			String res1 = lcs(a, b);
			int res2 = slowLcs(a, b);
			if (res1.length() != res2)
				throw new RuntimeException();
			int[] occurrences1 = occurrences(a, b);
			List<Integer> occurrences2 = new ArrayList<>();
			for (int p = a.indexOf(b); p != -1; p = a.indexOf(b, p + 1))
				occurrences2.add(p);
			if (!Arrays.equals(occurrences1, occurrences2.stream().mapToInt(Integer::intValue).toArray()))
				throw new RuntimeException();
		}
	}

	static String lcs(String a, String b) {
		State[] st = buildSuffixAutomaton(a);
		int bestState = 0;
		int len = 0;
		int bestLen = 0;
		int bestPos = -1;
		for (int i = 0, cur = 0; i < b.length(); ++i) {
			char c = b.charAt(i);
			if (st[cur].next[c] == -1) {
				for (; cur != -1 && st[cur].next[c] == -1; cur = st[cur].suffLink) {
				}
				if (cur == -1) {
					cur = 0;
					len = 0;
					continue;
				}
				len = st[cur].length;
			}
			++len;
			cur = st[cur].next[c];
			if (bestLen < len) {
				bestLen = len;
				bestPos = i;
				bestState = cur;
			}
		}
		return b.substring(bestPos - bestLen + 1, bestPos + 1);
	}

	static int[] occurrences(String haystack, String needle) {
		State[] automaton = buildSuffixAutomaton(haystack);
		int node = 0;
		for (char c : needle.toCharArray()) {
			int next = automaton[node].next[c];
			if (next == -1) {
				return new int[0];
			}
			node = next;
		}
		List<Integer> occurrences = new ArrayList<>();
		collectOccurrences(automaton, node, needle.length(), occurrences);
		return occurrences.stream().mapToInt(Integer::intValue).sorted().toArray();
	}

	static void collectOccurrences(State[] automaton, int curNode, int searchLength, List<Integer> occurrences) {
		if (automaton[curNode].firstPos != -1)
			occurrences.add(automaton[curNode].firstPos - searchLength + 1);
		for (int nextNode : automaton[curNode].invSuffLinks)
			collectOccurrences(automaton, nextNode, searchLength, occurrences);
	}

	static int slowLcs(String a, String b) {
		int[][] lcs = new int[a.length()][b.length()];
		int res = 0;
		for (int i = 0; i < a.length(); i++) {
			for (int j = 0; j < b.length(); j++) {
				if (a.charAt(i) == b.charAt(j))
					lcs[i][j] = 1 + (i > 0 && j > 0 ? lcs[i - 1][j - 1] : 0);
				res = Math.max(res, lcs[i][j]);
			}
		}
		return res;
	}

	static String getRandomString(int n, Random rnd) {
		return rnd.ints(n, 0, 3).mapToObj(i -> String.valueOf((char) (i + 'a'))).reduce("", (a, b) -> a + b);
	}
}
