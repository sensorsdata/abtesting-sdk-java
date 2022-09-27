package com.sensorsdata.analytics.javasdk;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 模拟服务端处理逻辑
 *
 * @author fangzhuo
 * @version 1.0.0
 * @since 2022/01/14 13:01
 */
public class TestServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String res = "{"
        + "    \"status\": \"SUCCESS\","
        + "    \"results\": ["
        + "        {"
        + "            \"abtest_experiment_id\": \"2\","
        + "            \"abtest_experiment_group_id\": \"1\","
        + "            \"is_control_group\": false,"
        + "            \"is_white_list\": false,"
        + "            \"experiment_type\": \"CODE\","
        + "            \"variables\": ["
        + "                {"
        + "                    \"name\": \"str_experiment\","
        + "                    \"type\": \"STRING\","
        + "                    \"value\": \"test\""
        + "                },"
        + "                {"
        + "                    \"name\": \"bool_experiment\","
        + "                    \"type\": \"BOOLEAN\","
        + "                    \"value\": \"false\""
        + "                },"
        + "                {"
        + "                    \"name\": \"int_experiment\","
        + "                    \"type\": \"INTEGER\","
        + "                    \"value\": \"123\""
        + "                },"
        + "                {"
        + "                    \"name\": \"json_experiment\","
        + "                    \"type\": \"JSON\","
        + "                    \"value\": \"{\\\"name\\\":\\\"hello\\\"}\""
        + "                }"
        + "            ]"
        + "        },"
        + "        {"
        + "            \"abtest_experiment_id\": \"14\","
        + "            \"abtest_experiment_group_id\": \"1\","
        + "            \"is_control_group\": false,"
        + "            \"is_white_list\": false,"
        + "            \"experiment_type\": \"CODE\","
        + "            \"variables\": ["
        + "                {"
        + "                    \"name\": \"int_abtest1\","
        + "                    \"type\": \"INTEGER\","
        + "                    \"value\": \"222\""
        + "                }"
        + "            ]"
        + "        },"
        + "        {"
        + "            \"abtest_experiment_id\": \"14\","
        + "            \"abtest_experiment_group_id\": \"-1\","
        + "            \"is_control_group\": false,"
        + "            \"is_white_list\": false,"
        + "            \"experiment_type\": \"CODE\","
        + "            \"variables\": ["
        + "                {"
        + "                    \"name\": \"test_group_id\","
        + "                    \"type\": \"JSON\","
        + "                    \"value\": \"{\\\"name\\\":\\\"helloWord\\\"}\""
        + "                }"
        + "            ]"
        + "        },"
        + "        {"
        + "            \"abtest_experiment_id\": \"14\","
        + "            \"abtest_experiment_group_id\": \"2\","
        + "            \"is_control_group\": false,"
        + "            \"is_white_list\": false,"
        + "            \"experiment_type\": \"CODE\","
        + "            \"variables\": ["
        + "                {"
        + "                    \"name\": \"test_group_id2\","
        + "                    \"type\": \"JSON\","
        + "                    \"value\": \"{\\\"name\\\":\\\"helloWord2\\\"}\""
        + "                }"
        + "            ]"
        + "        },"
        + "        {"
        + "            \"abtest_experiment_id\": \"14\","
        + "            \"abtest_experiment_group_id\": \"3\","
        + "            \"is_control_group\": false,"
        + "            \"is_white_list\": false,"
        + "            \"experiment_type\": \"CODE\","
        + "            \"variables\": ["
        + "                {"
        + "                    \"name\": \"test_group_id3\","
        + "                    \"type\": \"JSON\","
        + "                    \"value\": \"{\\\"name\\\":\\\"helloWord3\\\"}\""
        + "                }"
        + "            ]"
        + "        }"
        + "    ]"
        + "}";
    ServletOutputStream os = response.getOutputStream();
    os.write(res.getBytes(StandardCharsets.UTF_8));
    os.flush();
    os.close();
  }
}
