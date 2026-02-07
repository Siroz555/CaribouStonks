package fr.siroz.cariboustonks.util.http;

public record HttpResponse(
		int statusCode,
		String content
) implements AutoCloseable {

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean success() {
		return statusCode == 200;
	}

	@Override
	public void close() {
	}
}
