package am.ik.lognroll.logs.query;

import java.util.Iterator;

import am.ik.query.Node;
import am.ik.query.QueryParser;
import am.ik.query.RootNode;
import am.ik.query.TokenNode;
import am.ik.query.TokenType;

public class Sqlite3QueryConverter {

	public static String convertQuery(String query) {
		RootNode root = QueryParser.parseQuery(query);
		return convertQuery(root);
	}

	private static String convertQuery(RootNode root) {
		StringBuilder builder = new StringBuilder();
		Iterator<Node> iterator = root.children().iterator();
		while (iterator.hasNext()) {
			Node node = iterator.next();
			switch (node) {
				case TokenNode token -> {
					if (!builder.isEmpty()) {
						switch (token.type()) {
							case OR -> builder.append(" OR ");
							case EXCLUDE -> builder.append(" ");
							default -> builder.append(" AND ");
						}
					}
					if (token.type() == TokenType.OR) {
						if (iterator.hasNext()) {
							builder.append("\"").append(iterator.next().value()).append("\"");
						}
					}
					else if (token.type() == TokenType.EXCLUDE) {
						builder.append("NOT \"").append(token.value()).append("\"");
					}
					else {
						builder.append("\"").append(token.value()).append("\"");
					}
				}
				case RootNode nest -> {
					if (!builder.isEmpty()) {
						builder.append(" AND ");
					}
					builder.append("(").append(convertQuery(nest)).append(")");
				}
			}
		}
		return builder.toString();
	}

}
