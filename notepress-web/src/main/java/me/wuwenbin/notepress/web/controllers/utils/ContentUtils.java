package me.wuwenbin.notepress.web.controllers.utils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import lombok.Data;
import lombok.experimental.Accessors;
import me.wuwenbin.notepress.api.model.entity.Content;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 伍文彬
 * @date 2021-06-24 15:54
 **/
public class ContentUtils {


    /* 根据html内容计算出目录结构
     *
     * @param content
     * @return
     */
    public static ContentIncludeHeaders getContentIncludeHeaders(Content content) {

        String contentHtml = content.getHtmlContent();
        contentHtml = contentHtml.replace(" />", ">");
        Document document = Jsoup.parse(contentHtml);
        document.outputSettings().prettyPrint(false);

        //从 h1~h6 都包含进去
        for (int i = 1; i <= 6; i++) {
            Elements hs = document.getElementsByTag("h" + i);
            if (hs != null && hs.size() > 0) {
                for (int j = 0; j < hs.size(); j++) {
                    Element h = hs.get(j);
                    boolean hasIdAttr = h.hasAttr("id");
                    if (!hasIdAttr) {
                        String nano = "himio" + i + j;
                        h.attr("id", nano);
                    }
                }
            }
        }

        JXDocument doc = JXDocument.create(document);
        List<JXNode> nodes = doc.selN("//*");
        List<JXNode> headerNodes = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(nodes)) {
            for (JXNode node : nodes) {
                String nodeName = node.asElement().nodeName();
                if (Arrays.asList("h1", "h2", "h3", "h4", "h5", "h6").contains(nodeName)) {
                    if (StringUtils.isNotEmpty(node.asElement().text())) {
                        headerNodes.add(node);
                    }
                }
            }
        }
        List<Header> headers = headerNodes.stream()
                .map(n -> {
                    Header header = new Header();
                    String levelStr = n.asElement().nodeName().replace("h", "");
                    header.setLevel(Integer.parseInt(levelStr));
                    header.setTitle(n.selOne("/text()").value().toString());
                    header.setId(n.asElement().attr("id"));
                    header.setPosId(n.asElement().attr("id"));
                    return header;
                }).collect(Collectors.toList());

        content.setHtmlContent(Convert.toStr(doc.selOne("//body/html()")));
        return new ContentIncludeHeaders().setContent(content).setHeaders(headers);
    }

    @Data
    static class Header {
        private String title;
        private int level;
        private String id;
        private String posId;

    }

    @Data
    @Accessors(chain = true)
    public static class ContentIncludeHeaders {
        private List<Header> headers;
        private Content content;
    }
}
