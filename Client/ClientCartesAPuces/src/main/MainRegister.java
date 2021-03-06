package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

import org.json.JSONObject;

import smartcard.SmartCard;
import smartcard.Word;
import smartcard.WordSizeException;

public class MainRegister {

	public static void main(String[] args) {
		String urlString = "https://192.168.1.3:8443/AtelierBiometrie/AtelierBio";
		Scanner scan = new Scanner(System.in);
		Runtime runtime = Runtime.getRuntime();
		Process p;
		BufferedReader is, br;
		String line;
		
		// Demander login
		System.out.println("entrer votre login :");
		String login = scan.nextLine();
		
		// Demander mdp
		System.out.println("entrer votre mdp :");
		String mdp = scan.nextLine();
		
		// Demander nom
		System.out.println("entrer votre nom :");
		String lastname = scan.nextLine();
		
		// Demander prenom
		System.out.println("entrer votre pr�nom :");
		String firstname = scan.nextLine();
		
		// Demander mail
		System.out.println("entrer votre mail :");
		String mail = scan.nextLine();
		
		try {
			// Récupérer biométrie
//			p = new ProcessBuilder("./Hough_exec").start();
//			is = new BufferedReader(new InputStreamReader(p.getInputStream()));
//			
//			ArrayList<int[][]> histoList = new ArrayList<int[][]>();
//			int[][] histos = new int[3][];
//			int[] histoR = new int[256];
//			int[] histoG = new int[256];
//			int[] histoB = new int[256];
//			
//			p.waitFor();
//			
//			FileReader fr = new FileReader("./histo.txt");
//			
//			br = new BufferedReader(fr);
//
//			while ((line = br.readLine()) != null) {
//				System.out.println(line);
//				String[] values = line.split(",");
//				if(values.length == 257) {
//					switch(values[0]) {
//					case "R":
//						for(int i=0 ; i<256 ; i++) {
//							histoR[i] = Integer.valueOf(values[i+1]);
//						}
//						histos[0] = histoR;
//						break;
//					case "G":
//						for(int i=0 ; i<256 ; i++) {
//							histoG[i] = Integer.valueOf(values[i+1]);
//						}
//						histos[1] = histoG;
//						break;
//					case "B":
//						for(int i=0 ; i<256 ; i++) {
//							histoB[i] = Integer.valueOf(values[i+1]);
//						}
//						histos[2] = histoB;
//						break;
//					}
//				}
//				else {
//					System.out.println("Incorrect values length = " + values.length);
//				}
//				histoList.add(histos);
//			}
//			fr.close();	
//			new File("./histo.txt").delete();
//			Functions.trustSSL();

			String mdpSHA256 = Functions.stringToSHA256String(mdp);
			String urlParameters = "action=register&login=" + login + "&password=" + mdpSHA256 + "&lastName=" + lastname + "&firstName=" + firstname + "&mail=" + mail;

//			int countHisto = 1;
//			StringBuilder paramsBuilder = new StringBuilder();
//			String histoTemp = "";
//			int[][] histograms = histoList.get(0);
//
//			paramsBuilder.append("&histoR=");
//			histoTemp = "";
//			System.out.println(histograms[0].length);
//			for(int i=0 ; i<256 ; i++) {
//				histoTemp += ","+histograms[0][i];
//			}
//			paramsBuilder.append(histoTemp.substring(1));
//			
//			paramsBuilder.append("&histoG=");
//			histoTemp = "";
//			for(int i=0 ; i<256 ; i++) {
//				histoTemp += ","+histograms[1][i];
//			}
//			paramsBuilder.append(histoTemp.substring(1));
//			
//			paramsBuilder.append("&histoB=");
//			histoTemp = "";
//			for(int i=0 ; i<256 ; i++) {
//				histoTemp += ","+histograms[2][i];
//			}
//			paramsBuilder.append(histoTemp.substring(1));
//			
//			countHisto++;

			
//			urlParameters += paramsBuilder.toString();
			URL url = new URL(urlString+"?"+urlParameters);
			StringBuilder result = new StringBuilder();
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
	
			if (conn != null) {			
				System.out.println(url + " - " + conn.getResponseCode() + " " + conn.getResponseMessage());
				if (conn.getResponseCode() == 200) {
					br = new BufferedReader(
							new InputStreamReader(conn.getInputStream(), "UTF-8"));
	
					while ((line = br.readLine()) != null) {
						result.append(line + "\n");
					}
					br.close();
				}
				System.out.println(result.toString());

				// Récupérer id
				JSONObject json = new JSONObject(result.toString());
				
				String registerResult = json.getString("result");
				if(registerResult != null && registerResult.equals("ok")) {
					String id = json.getString("newUserID");
					
					// Ecrire dans la carte
					List<CardTerminal> terminauxDispos = TerminalFactory.getDefault().terminals().list();
					SmartCard sc = new SmartCard(terminauxDispos.get(0));
					// Attendre qu'il y ait une carte qui se connecte
					System.out.println("Attente d'une carte dans le terminal");
					sc.getTerminal().waitForCardPresent(0);
					sc.connect();
					System.out.println("Ecriture sur la carte");
					Word pin0 = new Word((byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA);
					ResponseAPDU r = sc.testPin((byte) 0x07, pin0);
					System.out.println(String.format("%02X %02X", r.getSW1(), r.getSW2()));
										
					sc.writeToCard((byte) 0x10, Functions.hexStringToByteArray(id));
				}
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (CardException e) {
			e.printStackTrace();
		} catch (WordSizeException e) {
			e.printStackTrace();
		}
		scan.close();
	}
}
