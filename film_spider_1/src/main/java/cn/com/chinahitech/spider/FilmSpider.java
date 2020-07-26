package cn.com.chinahitech.spider;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 爬取肖申克救赎的影评
 */
public class FilmSpider {
    private static Logger logger = LoggerFactory.getLogger(FilmSpider.class); //创建日志对象
    private static final String BASE_URL="https://movie.douban.com/top250?start=";
    private static final int PAGE_SIZE = 25 ;//每页条数

    /**
     * 根据指定网址，爬取页面内容  HttpClient
     * @param requestUrl 请求的网址
     */
    public void  requestByUrl(String requestUrl){
        System.out.println(requestUrl);
        //1)创建HttpClient对象（相当于浏览器）
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        //2)创建http请求对象，HttpGet对象
        HttpGet httpGet = new HttpGet(requestUrl);
        //2.1)给httpGet设置客户端类型
        httpGet.setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");

        //3)执行请求，获得响应
        try{
            CloseableHttpResponse response = httpClient.execute(httpGet);

            //4)获取响应状态行的信息
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() != 200){ //请求有问题
                return ; //程序终止
            }

            //5) 获取响应体中的数据
            HttpEntity httpEntity =response.getEntity();

            //6) 将HttpEntity ---> String
            String content = EntityUtils.toString(httpEntity);

            parseHtml(content);

        }catch(Exception ex){
            ex.printStackTrace();
        }

    }

    /**
     * 解析html内容  JSoup
     * @param content 待解析的html标签内容
     */
    public void parseHtml(String content){
        //1)使用jsoup 封装响应数据，--》Document 对象
        Document document = Jsoup.parse(content);

        //2)使用Jsoup API 提取该页面 20块  影评信息
        Elements elements = document.select("div > div.article > ol > li");
        //#content > div > div.article > ol #content > div > div.article > ol > li:nth-child(1)

        //3)解析数据
        System.out.println("电影名称、评分、评价人数、导演、编剧、主演、类型、制片国家/地区");
        for(Element item : elements){
            Element element = item.selectFirst("div.info");
            String movieName = element.selectFirst("span.title").text();
            String director=element.selectFirst("div.bd > p").text();
            String scope=element.select("div.bd > div.star >span.rating_num").text();
            String peopleNum=element.select("div.bd > div.star >span").last().text();
            System.out.println(movieName+","+director+","+scope+","+peopleNum);

            //5)内容拼接
            StringBuilder str = new StringBuilder();
            str.append(movieName).append(",").append(director).append(",").append(scope).append(",").append(peopleNum);

            //6)爬取到内容的写入
            logger.info(str.toString());
        }
    }

    /**
     * 爬取多页内容
     */
    public void requestByPages(int page){
        int beginIndex =0;
        for(int i = 0;i<page;i++){
            beginIndex = i*PAGE_SIZE;
            requestByUrl(BASE_URL+beginIndex);
            System.out.println();
        }
    }

    public static void main(String[] args)
    {
        FilmSpider spider = new FilmSpider();
        //spider.requestByUrl(BASE_URL);

        spider.requestByPages(10); //查看前5页
    }
}
