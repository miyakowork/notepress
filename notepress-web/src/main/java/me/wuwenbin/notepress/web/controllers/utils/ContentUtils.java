package me.wuwenbin.notepress.web.controllers.utils;

import cn.hutool.core.collection.CollectionUtil;
import org.apache.commons.lang3.StringUtils;
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
     * @param document
     * @return
     */
    public static List<Header> getHeadersByHtml(Document document) {

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
        return headerNodes.stream()
                .map(n -> {
                    Header header = new Header();
                    String levelStr = n.asElement().nodeName().replace("h", "");
                    header.setLevel(Integer.parseInt(levelStr));
                    header.setTitle(n.selOne("/text()").value().toString());
                    header.setId(n.asElement().attr("id"));
                    header.setPosId(n.asElement().attr("id"));
                    return header;
                }).collect(Collectors.toList());
    }

    static class Header {
        private String title;
        private int level;
        private String id;
        private String posId;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPosId() {
            return posId;
        }

        public void setPosId(String posId) {
            this.posId = posId;
        }

    }
}
