package crawler.client;

public class URLInfo {
    private String hostName;
    private int portNo;
    private String filePath;

    private boolean isHttps;

    /**
     * Constructor called with raw URL as input - parses URL to obtain host name
     * and file path
     */
    public URLInfo(String docURL) {
        if (docURL == null || docURL.equals(""))
            return;
        
        int idx = docURL.indexOf('?');
        if(idx != -1) {
        	docURL = docURL.substring(0, idx);
        }
        
        docURL = docURL.trim();

        // Stripping off 'http://'
        if (docURL.length() > 8 && docURL.startsWith("http://")) {
            docURL = docURL.substring(7);
        } else if (docURL.length() > 9 && docURL.startsWith("https://")) {
            docURL = docURL.substring(8);
            isHttps = true;
        }

        // If starting with 'www.' , stripping that off too
        // if(docURL.startsWith("www.")) docURL = docURL.substring(4);

        int i = 0;
        while (i < docURL.length()) {
            char c = docURL.charAt(i);
            if (c == '/')
                break;
            i++;
        }
        String address = docURL.substring(0, i);
        if (i == docURL.length())
            filePath = "/";
        else
            filePath = docURL.substring(i); // starts with '/'
        if (address.equals("/") || address.equals(""))
            return;
        if (address.indexOf(':') != -1) {
            String[] comp = address.split(":", 2);
            hostName = comp[0].trim();
            try {
                portNo = Integer.parseInt(comp[1].trim());
            } catch (NumberFormatException nfe) {
                portNo = isHttps ? 443 : 80;
            }
        } else {
            hostName = address;
            portNo = isHttps ? 443 : 80;
        }
    }

    public URLInfo(String hostName, String filePath) {
        this.hostName = hostName;
        this.filePath = filePath;
        this.portNo = 80;
    }

    public URLInfo(String hostName, int portNo, String filePath) {
        this.hostName = hostName;
        this.portNo = portNo;
        this.filePath = filePath;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String s) {
        hostName = s;
    }

    public int getPortNo() {
        return portNo;
    }

    public void setPortNo(int p) {
        portNo = p;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String fp) {
        filePath = fp;
    }

    public String toString() {
        return "host: " + hostName + ", file path: " + filePath + ", port number: " + portNo;
    }

    public static void main(String[] args) {
        System.out.println(new URLInfo("www.google.com"));
        System.out.println(new URLInfo("http://www.google.com"));
        System.out.println(new URLInfo("https://www.google.com"));
        System.out.println(new URLInfo("google.com/omg"));
        System.out.println(new URLInfo("https://www.google.com/ok"));
    }

}
