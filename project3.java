import java.io.*;
import java.util.*;

/*
    int[][] arrivalServiceTime is array for arrival and service time
    int size is total number of task
    List<String> Names is task names
*/
public class project3 {
    private int[][] arrivalServiceTime;  // arrival, service time array
    private int size; // total # of tasks
    private List<String> Names; // task names

    public project3(Queue<Integer> queue, List<String> Names) {
        this.size = queue.size();
        this.Names = Names;

        initialTaskTimes(queue);
    }

    /*
        This function will initialize arrival, service time array 
        queue is arrival and service time
     */
    private void initialTaskTimes(Queue<Integer> queue) {
        arrivalServiceTime = new int[size / 2][2];
        for (int i = 0; i < size / 2; i++) {
            arrivalServiceTime[i][0] = queue.poll();
            arrivalServiceTime[i][1] = queue.poll();
        }

    }

    /*
      choose the next task according to shortest process time algorithm
     */
    String chooseTask(boolean[] taskFinArr, int currentTim) {
        String selectedTask = "";
        int shortestServiceTime = Integer.MAX_VALUE;
        for (int i = 0; i < taskFinArr.length; i++) {
            if (!taskFinArr[i] && currentTim >= arrivalServiceTime[i][0]) {
                if (arrivalServiceTime[i][1] < shortestServiceTime) {
                    shortestServiceTime = arrivalServiceTime[i][1];
                    selectedTask = Names.get(i);
                }
            } else if (!taskFinArr[i] && currentTim < arrivalServiceTime[i][0]) {
                if (selectedTask.isEmpty())
                    selectedTask = Names.get(i);
                break;
            }
        }
        return selectedTask;
    }

    /*
      get the next selected task's index
     */
    private int SRTchooseTask(List<Integer> remainingTimeList, int curTime) {
        int shortestRemainTime = Integer.MAX_VALUE;
        int index = -1;
        for (int i = 0; i < remainingTimeList.size(); i++) {
            int remainT = remainingTimeList.get(i);
            if (remainT > 0 && remainT < shortestRemainTime && curTime >= arrivalServiceTime[i][0]) {
                shortestRemainTime = remainT;
                index = i;
            }
        }

        return index;
    }
    /*
    get the next task name
     */
    private String HrrnChooseTask(boolean[] taskFinArr, int currentTim) {
        String selectedTask = "";
        double highestRatio = Double.MIN_VALUE;
        for (int i = 0 ; i < taskFinArr.length; i++) {
            // calculate ration if task not excuted yet
            if (!taskFinArr[i] && currentTim >= arrivalServiceTime[i][0]) {
                double ratio = (currentTim - arrivalServiceTime[i][0] + arrivalServiceTime[i][1]) / (double) arrivalServiceTime[i][1];
                if (ratio > highestRatio) {
                    highestRatio = ratio;
                    selectedTask = Names.get(i);
                }
            } else if (!taskFinArr[i] && currentTim < arrivalServiceTime[i][0]) {
                if (selectedTask.isEmpty())
                    selectedTask = Names.get(i);
                break;
            }
        }

        return selectedTask;
    }

    /*
        This is function for FCFS algorithm
     */
    public Map<String, Queue<Integer>> FCFS() {
        Map<String, Queue<Integer>> mapingResult = new HashMap<>(); // store task scheduling time intervals

        int currentTim = arrivalServiceTime[0][0];
        for (int i = 0; i < arrivalServiceTime.length; i++) {
            String taskName = Names.get(i);
            Queue<Integer> timeIntervals = new LinkedList<>();
            timeIntervals.add(currentTim);
            timeIntervals.add(currentTim + arrivalServiceTime[i][1]);
            mapingResult.put(taskName, timeIntervals);

            if (i != arrivalServiceTime.length - 1)
                // this will compare current task and finish time with next arrival time
                currentTim = arrivalServiceTime[i + 1][0] > currentTim + arrivalServiceTime[i][1] ? 
                arrivalServiceTime[i + 1][0] : currentTim + arrivalServiceTime[i][1];
        }

        return mapingResult; // return map
    }

    /*
        RR algorithm
        time will starts from the first task's arrival time
     */
    public Map<String, Queue<Integer>> RB() {
        Map<String, Queue<Integer>> mapingResult = new HashMap<>();
        List<Integer> Time_ArrivalList = new ArrayList<>();
        List<Integer> Time_reaminList = new ArrayList<>();
        for (int i = 0; i < Names.size(); i++) {
            Time_ArrivalList.add(arrivalServiceTime[i][0]);
            Time_reaminList.add(arrivalServiceTime[i][1]);
        }

        String selectedTask = Names.get(0);
        int selectedTaskIdx = 0;
        int selectedTaskStartT = arrivalServiceTime[0][0];
        int time = arrivalServiceTime[0][0]; // time starts first task's arrival time
        int taskFinished = 0;
        
        // add the first task into queue
        Queue<String> taskQueue = new LinkedList<>();
        taskQueue.add(selectedTask);
        int idx = selectedTaskIdx;

        while (true) {
            String task = taskQueue.poll();
            if (task == null) { // this is when the previous task finished, but the next task doesn't arrive
                time++;
                if (Time_ArrivalList.contains(time)) {
                    int index = Time_ArrivalList.indexOf(time);
                    String nextTask = Names.get(index);
                    taskQueue.add(nextTask);  
                    while (true) {
                        index++;
                        if (index < Names.size() && Time_ArrivalList.get(index).equals(time)) {
                            String task1 = Names.get(index);
                            taskQueue.add(task1);
                        } else {
                            break;
                        }
                    }
                }
            } else {
                if (!task.equals(selectedTask)) {
                    if (Time_reaminList.get(selectedTaskIdx) != 0) { 
                        Queue<Integer> queue = mapingResult.get(selectedTask);
                        if (queue == null)
                            queue = new LinkedList<>();

                        // save previous task time
                        queue.add(selectedTaskStartT);
                        queue.add(time);
                        mapingResult.put(selectedTask, queue);
                    }

                    // update new task information
                    selectedTask = task;
                    selectedTaskIdx = Names.indexOf(selectedTask);
                    selectedTaskStartT = time;
                }

                // update remain time list
                int remainT = Time_reaminList.get(selectedTaskIdx);
                Time_reaminList.set(selectedTaskIdx, remainT - 1);
                time++; 
                
                // this will first add the new arrival task and add the current task back.
                if (Time_ArrivalList.contains(time)) {
                    int index = Time_ArrivalList.indexOf(time);
                    String nextTask = Names.get(index);
                    taskQueue.add(nextTask);
                    while (true) {
                        index++;
                        if (index < Names.size() && Time_ArrivalList.get(index).equals(time)) {
                            String task1 = Names.get(index);
                            taskQueue.add(task1);
                        } else {
                            break;
                        }
                    }
                }

                if (remainT - 1 == 0) {
                    // finished task's status will be saved here
                    Queue<Integer> queue = mapingResult.get(selectedTask);
                    if (queue == null)
                        queue = new LinkedList<>();

                    queue.add(selectedTaskStartT);
                    queue.add(time);
                    mapingResult.put(selectedTask, queue);

                    taskFinished++;
                    if (taskFinished == Names.size())
                        break;
                } else {
                    // if the task is not complete, add back to queue
                    taskQueue.add(selectedTask); 
                }
            }

        }

        return mapingResult;
    }

    /*
    SPN algorithm
    This function will return a scheduling time map related to each task
     */
    public Map<String, Queue<Integer>> SPN() {

        // store task scheduling time intervals
        Map<String, Queue<Integer>> mapingResult = new HashMap<>(); 
        boolean[] taskFinArr = new boolean[Names.size()];

        Queue<Integer> QueueTask = new LinkedList<>();
        QueueTask.add(arrivalServiceTime[0][0]);
        QueueTask.add(arrivalServiceTime[0][0] + arrivalServiceTime[0][1]);
        mapingResult.put(Names.get(0), QueueTask);
        int currentTim = arrivalServiceTime[0][0] + arrivalServiceTime[0][1];
        taskFinArr[0] = true;
        int taskFinished = 1;
        while (taskFinished < Names.size()) {
            String selectedTask = chooseTask(taskFinArr, currentTim);
            int index = Names.indexOf(selectedTask);
            if (arrivalServiceTime[index][0] > currentTim)
                currentTim = arrivalServiceTime[index][0];
            Queue<Integer> taskQueue = new LinkedList<>();
            taskQueue.add(currentTim);
            taskQueue.add(currentTim + arrivalServiceTime[index][1]);
            mapingResult.put(selectedTask, taskQueue);
            currentTim += arrivalServiceTime[index][1];
            taskFinArr[index] = true;
            taskFinished++;
        }

        return mapingResult;
    }



    /**
     SRT
     This function will return a scheduling time intervals map related to each task
     */
    public Map<String, Queue<Integer>> SRT() {
        Map<String, Queue<Integer>> mapingResult = new HashMap<>();

        List<Integer> Time_ArrivalList = new ArrayList<>();
        List<Integer> Time_reaminList = new ArrayList<>();
        for (int i = 0; i < arrivalServiceTime.length; i++) {
            Time_ArrivalList.add(arrivalServiceTime[i][0]);
            Time_reaminList.add(arrivalServiceTime[i][1]);
        }

        String selectedTask = Names.get(0);
        int selectedTaskIdx = 0;
        int selectedTaskStartT = arrivalServiceTime[0][0];
        int time = arrivalServiceTime[0][0];
        int taskFinished = 0;
        while (true) {
            if (Time_ArrivalList.contains(time)) {
                int newIndex = SRTchooseTask(Time_reaminList, time);
                if (selectedTaskIdx == -1) {
                    // update selected task info
                    selectedTaskIdx = newIndex;
                    selectedTask = Names.get(selectedTaskIdx);
                    selectedTaskStartT = time;
                } else if (newIndex != selectedTaskIdx) {
                    Queue<Integer> queue = mapingResult.get(selectedTask);
                    if (queue == null)
                        queue = new LinkedList<>();

                    // save previous task
                    queue.add(selectedTaskStartT);
                    queue.add(time);
                    mapingResult.put(selectedTask, queue);

                    // update selected task
                    selectedTaskIdx = newIndex;
                    selectedTask = Names.get(selectedTaskIdx);
                    selectedTaskStartT = time;
                }
            }

            time++; // time move forward

            if (selectedTaskIdx == -1)
                continue;
            int remainT = Time_reaminList.get(selectedTaskIdx);
            Time_reaminList.set(selectedTaskIdx, remainT - 1);
            if (remainT - 1 == 0) {
                // after finishe task it will be save here
                Queue<Integer> queue = mapingResult.get(selectedTask);
                if (queue == null) {
                    queue = new LinkedList<>();
                }
                // save previous task
                queue.add(selectedTaskStartT);
                queue.add(time);
                mapingResult.put(selectedTask, queue);

                taskFinished++;
                if (taskFinished == Names.size())
                    break;

                selectedTaskIdx = SRTchooseTask(Time_reaminList, time);
                if (selectedTaskIdx != -1) {
                    selectedTask = Names.get(selectedTaskIdx);
                    selectedTaskStartT = time;
                }
            }
        }

        return mapingResult;
    }


    /**
     * hrrn algorithm
     */
    public Map<String, Queue<Integer>> HRRN() {
        Map<String, Queue<Integer>> mapingResult = new HashMap<>();  // store task scheduling time intervals
        boolean[] taskFinArr = new boolean[Names.size()];

        Queue<Integer> QueueTask = new LinkedList<>();
        QueueTask.add(arrivalServiceTime[0][0]);
        QueueTask.add(arrivalServiceTime[0][0] + arrivalServiceTime[0][1]);
        mapingResult.put(Names.get(0), QueueTask);
        taskFinArr[0] = true;
        int currentTim = arrivalServiceTime[0][0] + arrivalServiceTime[0][1];
        //first task is finished
        int taskFinished = 1; 
        while (taskFinished < Names.size()) {
            String selectedTask = HrrnChooseTask(taskFinArr, currentTim);
            int index = Names.indexOf(selectedTask);
            if (arrivalServiceTime[index][0] > currentTim)
                currentTim = arrivalServiceTime[index][0];
            Queue<Integer> taskQueue = new LinkedList<>();
            taskQueue.add(currentTim);
            taskQueue.add(currentTim + arrivalServiceTime[index][1]);
            mapingResult.put(selectedTask, taskQueue);
            taskFinArr[index] = true;
             // update current time position
            currentTim += arrivalServiceTime[index][1];
            taskFinished++; 
        }

        return mapingResult;
    }
    /*
      FB algorithm

     */
    public Map<String, Queue<Integer>> FB() {
        Map<String, Queue<Integer>> mapingResult = new HashMap<>();
        Map<Integer, Queue<String>> queutPriority_map = new HashMap<>();

        Queue<String> priorQuest_1 = new LinkedList<>();
        Queue<String> priorQuest_2 = new LinkedList<>();
        Queue<String> priorQuest_3 = new LinkedList<>();

        queutPriority_map.put(1, priorQuest_1);
        queutPriority_map.put(2, priorQuest_2);
        queutPriority_map.put(3, priorQuest_3);
        Map<String, Integer> taskPriorityLevel = new HashMap<>();

        List<Integer> Time_ArrivalList = new ArrayList<>();
        List<Integer> Time_reaminList = new ArrayList<>();
        for (int i = 0; i < Names.size(); i++) {
            Time_ArrivalList.add(arrivalServiceTime[i][0]);
            Time_reaminList.add(arrivalServiceTime[i][1]);
            taskPriorityLevel.put(Names.get(i), 1); // initialize task priority level map
        }

        int time = arrivalServiceTime[0][0];
        String selectedTask = Names.get(0);
        int selectedTaskIdx = 0;
        int selectedTaskStartT = arrivalServiceTime[0][0];
        int taskFinished = 0;

        while (true) {
            if (Time_ArrivalList.contains(time)) {
                int index = Time_ArrivalList.indexOf(time);
                String task = Names.get(index);
                priorQuest_1.add(task);
                // TODO write into summary
                // in case there are multiple tasks arrive at the same time
                while (true) {
                    index++;
                    if (index < Names.size() && Time_ArrivalList.get(index).equals(time)) {
                        String task1 = Names.get(index);
                        priorQuest_1.add(task1);
                    } else
                        break;
                }
            }

            String newTask = "";
            if (!priorQuest_1.isEmpty()) {
                newTask = priorQuest_1.poll();
            } else if (!priorQuest_2.isEmpty()) {
                newTask = priorQuest_2.poll();
            } else if (!priorQuest_3.isEmpty()) {
                newTask = priorQuest_3.poll();
            }

            if (!newTask.isEmpty() && !newTask.equals(selectedTask)) {
                // move the selected to the next level queue
                if (Time_reaminList.get(selectedTaskIdx) > 0) {
                    Queue<Integer> queue = mapingResult.get(selectedTask);
                    if (queue == null)
                        queue = new LinkedList<>();

                    // save unfinish task status
                    queue.add(selectedTaskStartT);
                    queue.add(time);
                    mapingResult.put(selectedTask, queue);

                    int taskLevel = taskPriorityLevel.get(selectedTask);
                    if (taskLevel < 3) {
                        taskLevel++;
                        taskPriorityLevel.put(selectedTask, taskLevel);
                    }
                    Queue<String> taskQueue = queutPriority_map.get(taskLevel);
                    taskQueue.add(selectedTask);
                }

                selectedTask = newTask;
                selectedTaskIdx = Names.indexOf(selectedTask);
                selectedTaskStartT = time;

            }

            int remainT = Time_reaminList.get(selectedTaskIdx);
            time++;
            // if current task has finished, continue the next loop
            if (remainT == 0) 
                continue;

            Time_reaminList.set(selectedTaskIdx, remainT - 1);
            if (remainT - 1 == 0) {
                Queue<Integer> queue = mapingResult.get(selectedTask);
                if (queue == null)
                    queue = new LinkedList<>();

                // save finish task status 
                queue.add(selectedTaskStartT);
                queue.add(time);
                mapingResult.put(selectedTask, queue);

                taskFinished++;
                if (taskFinished == Names.size())
                    break;
            }
        }

        return mapingResult;
    }




    


    public static void main(String[] args) {
        String location_file = args[0];
        String command = args[1]; // algorithm name such as fcfs fb..
        

        try {
            FileInputStream fileIn = new FileInputStream(location_file);
            InputStreamReader inputStream = new InputStreamReader(fileIn);
            BufferedReader buffer_reader = new BufferedReader(inputStream);

            List<String> Names = new ArrayList<>();
            Queue<Integer> queue = new LinkedList<>();
            String line = buffer_reader.readLine();
            while (line != null) {
                String[] split = line.split("\\t");
                Names.add(split[0]);
                // TODO consider start time and service time will be float type???
                int arrivalTime = Integer.valueOf(split[1]);
                int serviceTime = Integer.valueOf(split[2]);
                queue.add(arrivalTime);
                queue.add(serviceTime);

                line = buffer_reader.readLine();
            }

            project3 sa = new project3(queue, Names);
            Map<String, Map<String, Queue<Integer>>> result = new HashMap<>();
            //call algorithm set default as upper case
            switch (command.toUpperCase()) {
                case "FCFS": {
                    Map<String, Queue<Integer>> mapingResult = sa.FCFS();
                    result.put("fcfs", mapingResult);
                    break;
                }
                case "RR": {
                    Map<String, Queue<Integer>> mapingResult = sa.RB();
                    result.put("rr", mapingResult);
                    break;
                }
                case "SPN": {
                    Map<String, Queue<Integer>> mapingResult = sa.SPN();
                    result.put("spn", mapingResult);
                    break;
                }
                case "SRT": {
                    Map<String, Queue<Integer>> mapingResult = sa.SRT();
                    result.put("srt", mapingResult);
                    break;
                }
                case "HRRN": {
                    Map<String, Queue<Integer>> mapingResult = sa.HRRN();
                    result.put("hrrn", mapingResult);
                    break;
                }
                case "FB": {
                    Map<String, Queue<Integer>> mapingResult = sa.FB();
                    result.put("fb", mapingResult);
                    break;
                }
                case "ALL":{
                    Map<String, Queue<Integer>> resultMapFCFS = sa.FCFS();
                    result.put("fcfs", resultMapFCFS);
                    Map<String, Queue<Integer>> resultMapRR = sa.RB();
                    result.put("rr", resultMapRR);
                    Map<String, Queue<Integer>> resultMapsSPN = sa.SPN();
                    result.put("spn", resultMapsSPN);
                    Map<String, Queue<Integer>> resultMapSRT = sa.SRT();
                    result.put("srt", resultMapSRT);
                    Map<String, Queue<Integer>> resultMapHRRN = sa.HRRN();
                    result.put("hrrn", resultMapHRRN);
                    Map<String, Queue<Integer>> resultMapFB = sa.FB();
                    result.put("fb", resultMapFB);
                    break;
                }
            }

            Set<String> keySet = result.keySet();
            for (String algorithmName : keySet) {
                System.out.println("----------------------------------");
                System.out.println("The scheduling of " + algorithmName + " is:");
                Map<String, Queue<Integer>> scheduleMap = result.get(algorithmName);

                for (int i = 0; i < scheduleMap.size(); i++) {
                    String taskName = Names.get(i);
                    System.out.print(taskName + " ");
                    Queue<Integer> timeIntervals = scheduleMap.get(taskName);
                    int prevTimeStamp = 0;
                    while (!timeIntervals.isEmpty()) {
                        int start = timeIntervals.poll();
                        int end = timeIntervals.poll();

                        // blank space for not execute
                        for (int j = 0; j < start - prevTimeStamp; j++)
                            System.out.print(" ");
                        // print X for execute
                        for (int j = 0; j < end - start; j++)
                            System.out.print("X");
                        prevTimeStamp = end; 
                    }
                    System.out.println();
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
