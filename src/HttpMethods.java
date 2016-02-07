
/**
 * Supported HTTP methods
 * @author Sharon David, Tal Bigel
 */
public enum HttpMethods {

		GET, POST, HEAD, TRACE, OPTIONS;

		/***
		 * Checks a given HTTP method, return error if not supported.
		 * 
		 * @param HTTP
		 *            Method string
		 * @return enum value of the HTTP method
		 * @throws InternalError
		 *             if method is not supported
		 */
		public static HttpMethods parseHTTPMethod(String method) {
			for (HttpMethods httpMethod : HttpMethods.values())
				if (method.toUpperCase().equals(httpMethod.toString())) {
					return httpMethod;
				}
			throw new InternalError("HTTP Method not supported!");
		}

		/**
		 * Create a string with all the supported methods, without OPTIONS
		 * 
		 * @return String of all methods except options
		 */
		public static String getMethodsString() {
			StringBuilder sb = new StringBuilder();
			HttpMethods[] httpMethods = HttpMethods.values();
			for (int i = 0; i < httpMethods.length - 1; i++) {
				sb.append(httpMethods[i].toString());
				sb.append(", ");
			}
			return sb.toString();
		}
}
