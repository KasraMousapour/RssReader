//package com.kasra;

import java.io.*;
import java.util.Scanner;

import org.jsoup.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.*;


public class RssReader {
    private static final int MAX_ITEMS = 5;
    FileReader fileReader;
    FileWriter fileWriter;
    BufferedReader bufferedReaderFile;
    BufferedWriter bufferedWriterFile;
    String websiteName;
    String websiteRSS;
    String lines;
    String[] linesArr;
    Scanner input = new Scanner(System.in);
    String websiteUrl;

    RssReader() {

        int temp;
        System.out.println("Welcome to RSS Reader!");
        while (true) {
            System.out.println("Type a valid number for your desired action:");
            System.out.println("[1] Show updates");
            System.out.println("[2] Add URL");
            System.out.println("[3] Remove URL");
            System.out.println("[4] Exit");
            try {
                int consoleNum = input.nextInt();
                if (consoleNum == 1) {
                    System.out.println("Show updates for:");
                    System.out.println("[0] All websites");
                    temp = 1;
                    try {
                        fileReader = new FileReader("data.txt");
                        bufferedReaderFile = new BufferedReader(fileReader);
                        while ((lines = bufferedReaderFile.readLine()) != null) {
                            linesArr = lines.split(";");
                            System.out.println("[" + (temp++) + "] " + linesArr[0]);
                        }
                        System.out.println("Enter -1 to return.");
                        bufferedReaderFile.close();
                    } catch (Exception e) {
                        System.out.println("Error in opening file!!");
                    }
                    int inputForShowUpdates = input.nextInt();
                    if (inputForShowUpdates == -1) continue;
                    else {
                        try {
                            fileReader = new FileReader("data.txt");
                            bufferedReaderFile = new BufferedReader(fileReader);
                            if (inputForShowUpdates == 0) {
                                while ((lines = bufferedReaderFile.readLine()) != null) {
                                    linesArr = lines.split(";");
                                    retrieveRssContent(linesArr[2]);
                                }
                            } else {
                                temp = 1;
                                while ((lines = bufferedReaderFile.readLine()) != null) {
                                    if (temp++ == inputForShowUpdates) {
                                        linesArr = lines.split(";");
                                        retrieveRssContent(linesArr[2]);
                                        break;
                                    }
                                }
                            }
                        } catch (IOException e) {
                            System.out.println("Error in opening file!!");
                        }
                    }


                } else if (consoleNum == 2) {
                    System.out.println("Please enter a website to add: ");
                    websiteUrl = input.next();
                    try {
                        fileReader = new FileReader("data.txt");
                        bufferedReaderFile = new BufferedReader(fileReader);
                        temp = 0;
                        while ((lines = bufferedReaderFile.readLine()) != null) {
                            linesArr = lines.split(";");
                            if (websiteUrl.equals(linesArr[1])) {
                                System.out.println(websiteUrl + " already exist.");
                                temp = 1;
                                break;
                            }
                        }
                        bufferedReaderFile.close();
                        if (temp == 0) {
                            try {
                                fileWriter = new FileWriter("data.txt",true);
                                bufferedWriterFile = new BufferedWriter(fileWriter);
                                websiteName = extractPageTitle(fetchPageSource(websiteUrl));
                                bufferedWriterFile.write('\n');
                                bufferedWriterFile.write(websiteName + ";");
                                bufferedWriterFile.write(websiteUrl + ";");
                                websiteRSS = extractRssUrl(websiteUrl);
                                bufferedWriterFile.write(websiteRSS);
                                System.out.println("Added " + websiteUrl + " successfully.");
                                bufferedWriterFile.close();
                            } catch (IOException e) {
                                System.out.println("Error in fileWriter handling!");
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("Error in fileReader handling!");
                    }

                } else if (consoleNum == 3) {
                    System.out.println("Please enter a website to remove: ");
                    websiteUrl = input.next();
                    try {
                        fileReader = new FileReader("data.txt");
                        bufferedReaderFile = new BufferedReader(fileReader);
                        temp = 1;
                        while ((lines = bufferedReaderFile.readLine()) != null) {
                            if (lines.contains(websiteUrl)) {
                                temp = 0;
                                break;
                            }
                        }
                        bufferedReaderFile.close();
                        if (temp == 0) {
                            File inputFile = new File("data.txt");
                            removeFromFile(inputFile, websiteUrl);
                        }
                    } catch (IOException e) {
                        System.out.println("Error in fileReader handling!");
                    } catch (Exception e) {
                        System.out.println("Error in removing from file!!");
                    }

                } else if (consoleNum == 4) {
                    break;
                }
            } catch (Exception e) {
                System.out.println("PLease Enter right number!!");
            }
        }
    }

    public static void main(String[] args) {
        new RssReader();
    }


    //gain html source from website URL
    public static String fetchPageSource(String urlString) throws Exception {
        URI uri = new URI(urlString);
        URL url = uri.toURL();
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML ,like Gecko) Chrome/108.0.0.0 Safari/537.36");
        return urlConnection.getInputStream().toString();
    }

    //gain website title from html source
    public static String extractPageTitle(String html) {
        try {
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            return  doc.select("title").first().text();
        } catch (Exception e) {
            return "Error: no title tag found in page source!";
        }
    }

    //gain RSS from Url
    public static void retrieveRssContent(String rssUrl) {
        try {
            String rssXml = fetchPageSource(rssUrl);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            StringBuilder xmlStringBuilder = new StringBuilder();
            xmlStringBuilder.append(rssXml);
            ByteArrayInputStream input = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
            org.w3c.dom.Document doc = documentBuilder.parse(input);
            NodeList itemNodes = doc.getElementsByTagName("item");
            for (int i = 0; i < MAX_ITEMS; ++i) {
                Node itemNode = itemNodes.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) itemNode;
                    System.out.println("Title: " + element.getElementsByTagName("title").item(0).getTextContent());
                    System.out.println("Link: " + element.getElementsByTagName("link").item(0).getTextContent());
                    System.out.println("Description: " + element.getElementsByTagName("description").item(0).
                            getTextContent());
                }
            }
        } catch (Exception e) {
            System.out.println("Error in retrieving RSS content for " + rssUrl + ": " + e.getMessage());
        }
    }


    //gain RSS address from URL
    public static String extractRssUrl(String url) throws IOException {
        org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
        return doc.select("[type='application/rss+xml']").attr("abs:href");
    }

    //removing a website from a file
    public static void removeFromFile(File inputFile, String websiteToRemove) throws Exception {

        File tempFile = new File("temp.txt");

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        PrintWriter writer = new PrintWriter(new FileWriter(tempFile));

        String line;

        while ((line = reader.readLine()) != null) {
            if (!line.contains(websiteToRemove)) {
                writer.println(line);
            }
        }

        reader.close();
        writer.close();

        // Delete the original file and rename the temporary file
        inputFile.delete();
        tempFile.renameTo(inputFile);
        System.out.println("Removed " + websiteToRemove + " successfully.");
    }

}
