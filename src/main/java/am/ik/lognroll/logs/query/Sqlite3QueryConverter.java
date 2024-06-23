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
							case EXCLUDE -> builder.append(" NOT ");
							case OR -> builder.append(" OR ");
							default -> builder.append(" AND ");
						}
					}
					if (token.type() == TokenType.OR) {
						if (iterator.hasNext()) {
							builder.append(iterator.next().value());
						}
					}
					else {
						builder.append(token.value());
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
