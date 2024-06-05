import java.io.*;
import java.util.*;

public class Main {
    public static double HuffmanSize;
    public static double RLESize;
    public static double LZWSize;

    public static void main(String[] args) throws IOException {
        System.out.println("\n======================== DNA Compression Tester ========================");
        System.out.println("This program  will test compare Huffman, RLE and Delta encoding methods");
        System.out.println("========================================================================\n");
        encode();
    }
    public static void encode() throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Please enter your Image/Video/Text Name (Include the .txt, .mp4, .wav) :");
        String inputFile = reader.readLine();
        System.out.println("Please enter your Output File Name (Include .txt or .bin) :");
        String outputFile = reader.readLine();

        encodeHuffMan(inputFile, outputFile);
        RLE(inputFile, outputFile);
        encodeLZW(inputFile, outputFile);
        System.out.println("\nAll files converted to DNA successfully!\n");
        System.out.println("Huffman: " + HuffmanSize + "\nRLE: " + RLESize + "\nLZW: " + LZWSize);
    }


    public static void encodeHuffMan(String inputFile, String outputFile){
        try {
            FileInputStream inputStream = new FileInputStream(inputFile);
            // Creates the array the size of the file
            byte[] data = new byte[(int) new File(inputFile).length()];
            // Puts the bytes into the array
            inputStream.read(data);
            inputStream.close();

            // Calculate byte frequencies
            Map<Byte, Integer> frequencyMap = new HashMap<>();
            for (byte b : data) {
                frequencyMap.put(b, frequencyMap.getOrDefault(b, 0) + 1);
            }

            // Sort the frequencyMap entries based on frequency (lowest frequency first)
            List<Map.Entry<Byte, Integer>> sortedEntries = new ArrayList<>(frequencyMap.entrySet());
            // Sort the entries based on the keys
            sortedEntries.sort(Comparator.comparingInt(Map.Entry::getValue));

            // Makes sure the lowest frequency is always at the top
            PriorityQueue<HuffmanNode> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(node -> node.frequency));
            for (Map.Entry<Byte, Integer> entry : sortedEntries) {
                priorityQueue.offer(new HuffmanNode(entry.getKey(), entry.getValue()));
            }

            // Build Huffman tree
            while (priorityQueue.size() > 1) {
                HuffmanNode left = priorityQueue.poll();
                HuffmanNode right = priorityQueue.poll();
                HuffmanNode parent = new HuffmanNode((byte) 0, left.frequency + right.frequency);
                parent.left = left;
                parent.right = right;
                priorityQueue.offer(parent);
            }

            HuffmanNode root = priorityQueue.poll();

            FileWriter treeWriter = new FileWriter("HuffManTree_" + outputFile);
            BufferedWriter bufferedTreeWriter = new BufferedWriter(treeWriter);

            // Generate Huffman codes
            Map<Byte, String> huffmanCodes = new HashMap<>();
            generateHuffmanCodes(root, "", huffmanCodes);

            // Write the Huffman codes to the file
            for (Map.Entry<Byte, String> entry : huffmanCodes.entrySet()) {
                byte dataHuff = entry.getKey();
                String huffmanCode = entry.getValue();
                bufferedTreeWriter.write(dataHuff + ":" + huffmanCode);
                bufferedTreeWriter.newLine();
            }

            bufferedTreeWriter.close();

            FileInputStream HuffmanTree = new FileInputStream("HuffManTree_" + outputFile);

            // Creates the array the size of the file
            byte[] data2 = new byte[(int) new File("HuffManTree_" + outputFile).length()];

            // Puts the bytes into the array
            HuffmanTree.read(data2);
            HuffmanTree.close();

            StringBuilder binaryString = new StringBuilder();

            for (Byte currentByte : data2) {
                // Convert each byte to binary representation
                String binaryByte = String.format("%8s", Integer.toBinaryString(currentByte & 0xFF)).replace(' ', '0');
                binaryString.append(binaryByte);
            }

            StringBuilder TreeDNA = new StringBuilder();
            for (int x = 0; x < binaryString.length() - 1; x += 2) {
                if (binaryString.charAt(x) == '0' && binaryString.charAt(x + 1) == '0') {
                    TreeDNA.append('A');
                } else if (binaryString.charAt(x) == '1' && binaryString.charAt(x + 1) == '0') {
                    TreeDNA.append('G');
                } else if (binaryString.charAt(x) == '0' && binaryString.charAt(x + 1) == '1') {
                    TreeDNA.append('C');
                } else if (binaryString.charAt(x) == '1' && binaryString.charAt(x + 1) == '1') {
                    TreeDNA.append('T');
                } else {
                    System.out.println("ERROR");
                }
            }

            TreeDNA.append("AGTTACGGGTACATTA");


            // Serialize the Huffman tree into a .txt file
            // Compress the binary data using Huffman codes
            StringBuilder compressedBinary = new StringBuilder();
            for (byte b : data) {
                compressedBinary.append(huffmanCodes.get(b));
            }

            bufferedTreeWriter.close();

            int lastDigitIndex = compressedBinary.length() - 1;

            // Converts the compressed binary to DNA
            StringBuilder DNA = new StringBuilder();
            for (int x = 0; x < compressedBinary.length() - 1; x += 2) {
                if (compressedBinary.charAt(x) == '0' && compressedBinary.charAt(x + 1) == '0') {
                    DNA.append('A');
                } else if (compressedBinary.charAt(x) == '1' && compressedBinary.charAt(x + 1) == '0') {
                    DNA.append('G');
                } else if (compressedBinary.charAt(x) == '0' && compressedBinary.charAt(x + 1) == '1') {
                    DNA.append('C');
                } else if (compressedBinary.charAt(x) == '1' && compressedBinary.charAt(x + 1) == '1') {
                    DNA.append('T');
                } else {
                    System.out.println("ERROR");
                }
            }


            //Checks if odd so the last byte does not get unaccounted for
            if (compressedBinary.length() % 2 == 1) {
                if (compressedBinary.charAt(lastDigitIndex) == '0') {
                    DNA.append("TAGTAAC"); // Using TAGTAAC as a placeholder for odd 0
                } else {
                    DNA.append("ACTGTCC"); // Using ACTGTCC as a placeholder for odd 1
                }
            }

            // Writes the DNA to the file (.txt or .bin)
            FileWriter dnaWriter = new FileWriter("HuffMan_" + outputFile);
            dnaWriter.write(TreeDNA.toString());
            dnaWriter.write(DNA.toString());
            dnaWriter.close();

            //Calculate the size of the outputFile

            File file = new File("Huffman_" + outputFile);
            double bytes = file.length();
            double kilobytes = (bytes / 1024);

            // Save the size in a variable (optional, if needed for further processing)
            HuffmanSize = kilobytes;

            System.out.println("\nHuffman encoding method successful!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void RLE(String inputFile, String outputFile) {
        try {
            FileInputStream inputStream = new FileInputStream(inputFile);
            FileWriter outputStream = new FileWriter("RLE_" + outputFile);

            // Creates the array the size of the file
            byte[] data = new byte[(int) new File(inputFile).length()];

            // Puts the bytes into the array
            inputStream.read(data);
            inputStream.close();

            // Variables to keep track of the current run and value
            int currentRun = 1;
            byte currentValue = data[0];

            // Apply RLE for matching numbers based on the rules
            List<Byte> RLEBytes = new ArrayList<>();
            for (int i = 1; i < data.length; i++) {
                if (data[i] == currentValue) {
                    currentRun++;
                } else {
                    if (currentRun >= 3) {
                        RLEBytes.add((byte) -currentRun); // Store run length as negative value
                    }
                    else if(currentRun > 1){
                        RLEBytes.add(currentValue);
                    }
                    RLEBytes.add(currentValue); // Store value as positive value
                    currentRun = 1;
                    currentValue = data[i];
                }
            }
            // Write the last run (if any)
            if (currentRun >= 3) {
                RLEBytes.add((byte) -currentRun); // Store run length as negative value
            }
            else if(currentRun > 1){
                RLEBytes.add(currentValue);
            }
            RLEBytes.add(currentValue); // Store value as positive value

            // Convert RLE bytes to binary and then to DNA
            StringBuilder binaryString = new StringBuilder();
            for (Byte currentByte : RLEBytes) {
                // Convert each byte to binary representation
                String binaryByte = String.format("%8s", Integer.toBinaryString(currentByte & 0xFF)).replace(' ', '0');
                binaryString.append(binaryByte);
            }

            // Convert binary to DNA
            StringBuilder dnaString = new StringBuilder();
            for (int i = 0; i < binaryString.length(); i += 2) {
                String codon = binaryString.substring(i, i + 2);

                // Map binary digits to DNA nucleotides
                if (codon.equals("00")) {
                    dnaString.append("A");
                } else if (codon.equals("01")) {
                    dnaString.append("G");
                } else if (codon.equals("10")) {
                    dnaString.append("C");
                } else if (codon.equals("11")) {
                    dnaString.append("T");
                }
            }

            // Write the DNA bytes to the output file
            outputStream.write(dnaString.toString());
            outputStream.close();

            //Calculate the size of the outputFile

            File file = new File("RLE_" + outputFile);
            double bytes = file.length();
            double kilobytes = (bytes / 1024);

            // Save the size in a variable (optional, if needed for further processing)
            RLESize = kilobytes;

            System.out.println("RLE encoding method successful!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<List<Byte>, Integer> dictionary = new HashMap<>();

    public static void encodeLZW(String inputFile, String outputFile) {
        try {
            FileInputStream inputStream = new FileInputStream(inputFile);
            FileWriter outputStream = new FileWriter("LZW_" + outputFile);

            byte[] data = new byte[(int) new File(inputFile).length()];
            inputStream.read(data);
            inputStream.close();

            List<Byte> currentPattern = new ArrayList<>();
            List<Integer> encodedData = new ArrayList<>();

            for (int i = 0; i < 256; i++) {
                List<Byte> key = new ArrayList<>();
                key.add((byte) i);
                dictionary.put(key, i);
            }

            for (int i = 0; i < data.length; i++) {
                byte currentByte = data[i];
                List<Byte> extendedPattern = new ArrayList<>(currentPattern);
                extendedPattern.add(currentByte);

                if (dictionary.containsKey(extendedPattern)) {
                    currentPattern = extendedPattern;
                } else {
                    encodedData.add(dictionary.get(currentPattern));

                    dictionary.put(extendedPattern, dictionary.size());

                    currentPattern = new ArrayList<>();
                    currentPattern.add(currentByte);
                }
            }

            if (!currentPattern.isEmpty()) {
                encodedData.add(dictionary.get(currentPattern));
            }

            exportDictionary("dictionary.txt");
            FileInputStream HuffmanTree = new FileInputStream("dictionary.txt");

            // Creates the array the size of the file
            byte[] data2 = new byte[(int) new File("dictionary.txt").length()];

            // Puts the bytes into the array
            HuffmanTree.read(data2);
            HuffmanTree.close();

            StringBuilder binaryString = new StringBuilder();

            for (Byte currentByte : data2) {
                // Convert each byte to binary representation
                String binaryByte = String.format("%8s", Integer.toBinaryString(currentByte & 0xFF)).replace(' ', '0');
                binaryString.append(binaryByte);
            }

            StringBuilder TreeDNA = new StringBuilder();
            for (int x = 0; x < binaryString.length() - 1; x += 2) {
                if (binaryString.charAt(x) == '0' && binaryString.charAt(x + 1) == '0') {
                    TreeDNA.append('A');
                } else if (binaryString.charAt(x) == '1' && binaryString.charAt(x + 1) == '0') {
                    TreeDNA.append('G');
                } else if (binaryString.charAt(x) == '0' && binaryString.charAt(x + 1) == '1') {
                    TreeDNA.append('C');
                } else if (binaryString.charAt(x) == '1' && binaryString.charAt(x + 1) == '1') {
                    TreeDNA.append('T');
                } else {
                    System.out.println("ERROR");
                }
            }

            TreeDNA.append("AGTTACGGGTACATTA");

            outputStream.write(TreeDNA.toString());

            StringBuilder sb = new StringBuilder();

            for (Integer code : encodedData) {
                sb.append(String.format("%8s", Integer.toBinaryString(code & 0xFF)).replace(' ', '0'));
            }

            String DNA = sb.toString();

            for (int i = 0; i < sb.length() - 1; i += 2) {
                if (DNA.charAt(i) == '0' && DNA.charAt(i + 1) == '0') {
                    outputStream.write('A');
                } else if (DNA.charAt(i) == '0' && DNA.charAt(i + 1) == '1') {
                    outputStream.write('C');
                } else if (DNA.charAt(i) == '1' && DNA.charAt(i + 1) == '0') {
                    outputStream.write('G');
                } else if (DNA.charAt(i) == '1' && DNA.charAt(i + 1) == '1') {
                    outputStream.write('T');
                } else {
                    System.out.println("INVALID NUCLEOTIDE");
                }
            }

            outputStream.close();

            File file = new File("LZW_" + outputFile);
            double bytes = file.length();
            double kilobytes = (bytes / 1024);

            LZWSize = kilobytes;

            System.out.println("LZW encoding method successful!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void exportDictionary(String filename) {
        try {
            FileWriter writer = new FileWriter(filename);

            for (Map.Entry<List<Byte>, Integer> entry : dictionary.entrySet()) {
                List<Byte> key = entry.getKey();
                Integer value = entry.getValue();
                StringBuilder keyStr = new StringBuilder();

                for (Byte b : key) {
                    keyStr.append(b).append(" ");
                }

                writer.write(keyStr.toString() + "-> " + value + "\n");
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void generateHuffmanCodes(HuffmanNode node, String code, Map<Byte, String> huffmanCodes) {
        if (node == null) {
            return;
        }

        if (node.left == null && node.right == null) {
            huffmanCodes.put(node.data, code);
        }

        generateHuffmanCodes(node.left, code + "0", huffmanCodes);
        generateHuffmanCodes(node.right, code + "1", huffmanCodes);
    }

    private static class HuffmanNode implements Comparable<HuffmanNode> {
        byte data;
        int frequency;
        HuffmanNode left;
        HuffmanNode right;

        public HuffmanNode(byte data, int frequency) {
            this.data = data;
            this.frequency = frequency;
        }

        @Override
        public int compareTo(HuffmanNode other) {
            return this.frequency - other.frequency;
        }
    }

}