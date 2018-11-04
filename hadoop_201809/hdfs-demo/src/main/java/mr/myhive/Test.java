package mr.myhive;

import java.util.Arrays;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/9 8:27
 */
public class Test {

    @org.junit.Test
    public void test1() {
        Student s1 = new Student();
        s1.setAge(1);

        Student s2 = new Student();
        s2.setAge(4);

        Student[] students = new Student[2];
        students[1]=s1;
        students[0]=s2;

        int[] ints = {5,4,3,2,1};
        Arrays.sort(ints);
        System.out.println(Arrays.toString(ints));

        Arrays.sort(students,new Student());
        System.out.println(Arrays.toString(students));
    }
}
