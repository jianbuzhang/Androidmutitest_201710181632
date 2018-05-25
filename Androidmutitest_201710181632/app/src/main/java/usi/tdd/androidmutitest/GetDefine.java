package usi.tdd.androidmutitest;


import android.graphics.Bitmap;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Admin on 2016/8/25.
 */
public class GetDefine {
    //public static String ConfigFile = "storage/IPSM/MFGDefine.xml"; //define *.xml path
    public static String ConfigFile = "storage/emulated/0/MFGDefine.xml"; //define *.xml path


    public static String getValue(String Session, String Key, String attr) {
        String value = "";
        try {
            InputStream inStream = new FileInputStream(ConfigFile);  //讀取檔案

            Log.v("XML_RW", "Read xml file");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inStream);  //以樹狀格式存於記憶體中﹐比較耗記憶體
            Element root = document.getDocumentElement(); //取得檔案的"根"標籤

            NodeList nodes = root.getElementsByTagName(Session); //尋找"根"標籤下的目標名稱的物件並製作成表
            if (nodes.getLength() < 1) {
                Log.d("MFG_TEST", "No " + Session + " setting found!!");
            } else {
                Element sessionElement = (Element) nodes.item(0);
                NodeList key2 = sessionElement.getElementsByTagName(Key);

                if (key2.getLength() < 1) {
                    Log.d("MFG_TEST", "No " + Key + " Delay found!!");
                } else {
                    value = ((Element) key2.item(0)).getAttribute(attr);
                }
            }
        } catch (Exception er) {
            Log.e("XML_RW", er.getMessage());
        }

        return value;
    }

    public static int getValueInt(String Session, String Key, String attr) {
        String value = getValue(Session, Key, attr);
        if ("".equals(value)) {
            return (Integer) null;
        } else {
            return Integer.parseInt(value);
        }
    }

    public static void saveToFile(Bitmap bmp, String filePath) {
        File DrawFile = new File(filePath);
        BufferedOutputStream bufOutStr = null;
        try {
            bufOutStr = new BufferedOutputStream(new FileOutputStream(DrawFile));
            bmp.compress(Bitmap.CompressFormat.PNG, 90, bufOutStr);
            bufOutStr.flush();
            bufOutStr.close();
            Log.d("MFG_TEST", "Save File SUCCESSFUL!!");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d("MFG_TEST", "Save File Error!!");
        }

    }


    public static KeyDefine[] getKeys(String conf, String type) {
        KeyDefine Keys[] = null;
        try {
            InputStream inStream = new FileInputStream(ConfigFile);  //讀取檔案

            Log.v("XML_RW", "Read xml file");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inStream);  //以樹狀格式存於記憶體中﹐比較耗記憶體
            Element root = document.getDocumentElement(); //取得檔案的"根"標籤

            NodeList nodes = root.getElementsByTagName(conf);

            if (nodes.getLength() < 1) {
                Log.d("MFG_TEST", "No key setting found!!");
            } else {
                Element sessionElement = (Element) nodes.item(0);
                NodeList node2 = sessionElement.getElementsByTagName(type);

                if (node2.getLength() < 1) {
                    Log.d("MFG_TEST", "No " + type + " found!!");
                } else {
                    Element typeElement = (Element) node2.item(0);
                    NodeList buts = typeElement.getElementsByTagName("button");
                    if (buts.getLength() > 0) {
                        int nLen = buts.getLength();
                        Keys = new KeyDefine[nLen];
                        for (int nIdx = 0; nIdx < nLen; nIdx++) {
                            Keys[nIdx] = new KeyDefine();
                            Keys[nIdx].caption = ((Element) buts.item(nIdx)).getAttribute("caption");
                            Keys[nIdx].code = Integer.parseInt(((Element) buts.item(nIdx)).getAttribute("code"));
                            Log.d("MFG_TEST", Keys[nIdx].caption);
                        }
                    }
                }
            }

        } catch (Exception er) {
            Log.d("MFG_TEST", "getKey Error: " + er.getMessage());
        }
        return Keys;
    }


    public static KeyDefine[][] getCenterKeys(String conf) {
        KeyDefine Keys[][] = null;
        try {
            InputStream inStream = new FileInputStream(ConfigFile); //讀取檔案

            Log.v("XML_RW", "Read xml file");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inStream);  //以樹狀格式存於記憶體中﹐比較耗記憶體
            Element root = document.getDocumentElement(); //取得檔案的"根"標籤

            NodeList nodes = root.getElementsByTagName(conf); //尋找"根"標籤下的目標名稱的物件並製作成表

            if (nodes.getLength() < 1) {
                Log.d("MFG_TEST", "No key setting found!!");
            } else {
                Element sessionElement = (Element) nodes.item(0);
                NodeList node2 = sessionElement.getElementsByTagName("center");

                if (node2.getLength() < 1) {
                    Log.d("MFG_TEST", "No center found!!");
                } else {
                    Element typeElement = (Element) node2.item(0);
                    NodeList rows = typeElement.getElementsByTagName("row");
                    if (rows.getLength() > 0) {
                        int nRow = rows.getLength();
                        Keys = new KeyDefine[nRow][];
                        for (int nIdx = 0; nIdx < nRow; nIdx++) {
                            Element rowElement = (Element) rows.item(nIdx);
                            NodeList bts = rowElement.getElementsByTagName("button");
                            if (bts.getLength() > 0) {
                                Keys[nIdx] = new KeyDefine[bts.getLength()];
                                for (int nJdx = 0; nJdx < bts.getLength(); nJdx++) {
                                    Keys[nIdx][nJdx] = new KeyDefine();
                                    Keys[nIdx][nJdx].caption = ((Element) bts.item(nJdx)).getAttribute("caption");
                                    Keys[nIdx][nJdx].code = Integer.parseInt(((Element) bts.item(nJdx)).getAttribute("code"));
                                    Keys[nIdx][nJdx].img = ((Element) bts.item(nJdx)).getAttribute("img");
                                    if (!((Element) bts.item(nJdx)).getAttribute("width").equals(""))
                                        Keys[nIdx][nJdx].width = Integer.parseInt(((Element) bts.item(nJdx)).getAttribute("width"));
                                    if (!((Element) bts.item(nJdx)).getAttribute("height").equals(""))
                                        Keys[nIdx][nJdx].heigth = Integer.parseInt(((Element) bts.item(nJdx)).getAttribute("height"));
                                    if (!((Element) bts.item(nJdx)).getAttribute("hot").equals(""))
                                        Keys[nIdx][nJdx].hok = ((Element) bts.item(nJdx)).hasAttribute("hok");
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception er) {
            //  Log.e("XML_RW", );
            Log.d("MFG_TEST", "getCenterKey Error: " + er.getMessage());
        }
        return Keys;
    }
}
