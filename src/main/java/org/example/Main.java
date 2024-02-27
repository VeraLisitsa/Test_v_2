package org.example;

import java.io.*;
import java.util.*;

public class Main {

    private static Set<Map<Integer, String>> setUniqueStr; // набор всех уникальных строчек (списки элементов)
    private static List<Map<Integer, String>> listUniqueStr; //список всех уникальных строчек
    private static Set<Set<Integer>> allGroups; //набор групп номеров строчек
    private static List<Map<String, Set<Integer>>> listMapNumberPosition; // набор мап с наборами зачение одного столбца
    private static NavigableSet<Integer> setAllNumbersStr; //набор номеров строк из listUniqueStr
    private static Map<Integer, Set<Set<Integer>>> mapSetsStrWithSize; // мапа, где ключ - размер непересекающейся группы строк, значение - набор групп строк

    private static int lengthMax; //максимальная длина строки

    private static final String DELIMITER = ";";

    private static final String SAMPLESTR = "\".*\"";

    private static String inputFileName;

    private static String outputFileName = "out.txt";


    public static void main(String[] args) throws IOException {
        long begin = System.currentTimeMillis();
        inputFileName = args[0];
        setUniqueStr = new HashSet<>();
        lengthMax = 0;


        //считываем строки из файла, помещаем в set

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] arr = line.split(DELIMITER);
                if (arr.length == 0) {
                    continue;
                }

                if (lengthMax < arr.length) {
                    lengthMax = arr.length;
                }

                Map<Integer, String> mapStr = new HashMap<>();
                boolean isWrongStr = false;
                for (int i = 0; i < arr.length; i++) {
                    if (!arr[i].equals("") && !arr[i].equals("\"\"")&& !arr[i].matches(SAMPLESTR)) {
                        isWrongStr = true;
                        break;
                    }
                    if (!arr[i].equals("") && !arr[i].equals("\"\"")) {
                        mapStr.put(i, arr[i]);
                    }
                }

                if (!isWrongStr && !mapStr.isEmpty()) {
                    setUniqueStr.add(mapStr);
                }
            }
        }


        //переводим набор срок в список
        listUniqueStr = new ArrayList<>(setUniqueStr);

        setUniqueStr.clear();


        //разбиваем все столбцы на мапы, номер мапы - номер столбца (начиная с 0), ключ - элемент,
        //значение - набор их номеров строк из  listUniqueStr, где это значение
        //встречается в столбце = номеру мапы
        //мапы добавляются по порядку в список listMapNumberPosition

        listMapNumberPosition = new ArrayList<>();

        generatingColumnsMaps();

        //создание сортированного набора всех строк из списка listUniqueStr
        setAllNumbersStr = new TreeSet<>();
        for (
                int i = 0; i < listUniqueStr.size(); i++) {
            setAllNumbersStr.add(i);
        }

        //объединение строк в непересекающиеся группы
        groupUnion();

        System.out.println("Число уникальных групп - " + allGroups.size());

        //формирование мапы, где ключ - размер непересекающейся группы строк,
        // значение - набор непересекающихся групп соответствующего размера

        generatingMapSetsStr();

        //вывод результата в файл
        writeToFile(outputFileName);

        long end = System.currentTimeMillis();

        System.out.println("Затрачено: " + (end - begin) + "мс");

    }


    private static void generatingColumnsMaps() {
        for (int i = 0; i < lengthMax; i++) {
            Map<String, Set<Integer>> map = new HashMap<>();

            for (int j = 0; j < listUniqueStr.size(); j++) {

                if (listUniqueStr.get(j).containsKey(i)) {
                    if (!map.containsKey(listUniqueStr.get(j).get(i))) {
                        Set<Integer> setNumberStr = new HashSet<>();
                        setNumberStr.add(j);
                        map.put(listUniqueStr.get(j).get(i), setNumberStr);
                    } else {
                        map.get(listUniqueStr.get(j).get(i)).add(j);
                    }
                }

            }
            listMapNumberPosition.add(map);
        }
    }

    private static void groupUnion() {

        Queue<Integer> queueForAddInGroup = new LinkedList<>();
        allGroups = new HashSet<>();

        while (!setAllNumbersStr.isEmpty()) {
            int numberStr = setAllNumbersStr.first();

            Set<Integer> group = new HashSet<>();

            queueForAddInGroup.add(numberStr);

            while (!queueForAddInGroup.isEmpty()) {
                int numberStrNext = queueForAddInGroup.poll();
                group.add(numberStrNext);
                setAllNumbersStr.remove(numberStrNext);
                for (Map.Entry<Integer, String> entry : listUniqueStr.get(numberStrNext).entrySet()) {
                    String value = entry.getValue();
                    Integer numberMap = entry.getKey();
                    Set<Integer> setStr = listMapNumberPosition.get(numberMap).get(value);
                    setStr.retainAll(setAllNumbersStr);
                    queueForAddInGroup.addAll(setStr);
                    setStr.clear();
                }
            }
            if (group.size() > 1) {
                allGroups.add(group);
            }
        }
    }

    private static void generatingMapSetsStr() {
        mapSetsStrWithSize = new TreeMap<>(Collections.reverseOrder());
        for (Set<Integer> setStr : allGroups) {
            int sizegroup = setStr.size();
            if (!mapSetsStrWithSize.containsKey(sizegroup)) {
                Set<Set<Integer>> setsOnesize = new HashSet<>();
                setsOnesize.add(setStr);
                mapSetsStrWithSize.put(sizegroup, setsOnesize);
            } else {
                Set<Set<Integer>> setsOnesize = mapSetsStrWithSize.get(sizegroup);
                setsOnesize.add(setStr);
            }
        }
    }

    private static void writeToFile(String outputFileName) throws IOException {
        int numberGroup = 1;

        try (BufferedWriter writter = new BufferedWriter(new FileWriter(outputFileName))) {
            writter.write("Число групп с более чем одним элементом - " + allGroups.size() + "\n");
            for (Map.Entry<Integer, Set<Set<Integer>>> entry : mapSetsStrWithSize.entrySet()) {
                for (Set<Integer> group : entry.getValue()) {
                    writter.write("Группа " + numberGroup + "\n");
                    numberGroup++;
                    for (Integer numberStr : group) {
                        Map<Integer, String> str = listUniqueStr.get(numberStr);
                        for (int i = 0; i < listMapNumberPosition.size(); i++) {
                            if (str.containsKey(i)) {
                                writter.write((i == (listMapNumberPosition.size() - 1)) ? str.get(i) + "\n" : str.get(i) + DELIMITER);
                            } else {
                                writter.write((i == (listMapNumberPosition.size() - 1)) ? "\n" : DELIMITER);
                            }
                        }
                    }
                }
            }
        }
    }

}