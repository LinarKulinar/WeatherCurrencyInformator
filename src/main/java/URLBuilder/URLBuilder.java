package URLBuilder;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class URLBuilder {
    private String url;
    private List<String> params;

    public URLBuilder() {
        url = "";
        params = new ArrayList<>();
    }

    public URLBuilder(String url) {
        this();
        this.url = url;
    }

    public URLBuilder withProtocol(String protocol) throws IllegalStateException {
        if (!url.isEmpty())
            throw new IllegalStateException("Метод должен вызываться первым");
        url = protocol + "://";
        return this;
    }

    public URLBuilder withHost(String host) {
        if (url.isEmpty())
            throw new IllegalStateException("Не определён протокол");
        url += host;
        return this;
    }

    public URLBuilder withPath(String ... path) {
        if (!url.endsWith("/"))
            url += "/";
        if (path.length > 0)
            url += String.join("/", path);
        else
            url += path;
        return this;
    }

    public URLBuilder withParam(String name, String value) throws UnsupportedEncodingException {
        String charset = Charset.defaultCharset().name();
        String encodedName = URLEncoder.encode(name, charset);
        String encodedValue = URLEncoder.encode(value, charset);
        params.add(encodedName + "=" + encodedValue);
        return this;
    }

    public URL build() throws MalformedURLException {
        return new URL(this.toString());
    }

    @Override
    public String toString() {
        return url + "?" + String.join("&", params);
    }
}