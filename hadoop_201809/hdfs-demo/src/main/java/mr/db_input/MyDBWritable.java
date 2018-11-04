package mr.db_input;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.lib.db.DBWritable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Description:
 * @author: HuangYn
 * @date: 2018/9/6 15:55
 */
public class MyDBWritable implements DBWritable, Writable {

    private int id;
    private String name;
    private String text;

    //
    private String word;
    private int count;

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(id);
        out.writeUTF(name);
        out.writeUTF(text);
        out.writeUTF(word);
        out.writeInt(count);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        id = in.readInt();
        name = in.readUTF();
        text = in.readUTF();
    }

    //写入database
    @Override
    public void write(PreparedStatement statement) throws SQLException {
        statement.setString(1, word);
        statement.setInt(2, count);
    }

    //从database中读取
    @Override
    public void readFields(ResultSet resultSet) throws SQLException {

        id = resultSet.getInt(1);
        name = resultSet.getString(2);
        text = resultSet.getString(3);

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "MyDBWritable{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", text='" + text + '\'' +
                '}';
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
