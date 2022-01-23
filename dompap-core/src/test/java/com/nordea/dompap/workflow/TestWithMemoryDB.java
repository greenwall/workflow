package com.nordea.dompap.workflow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

//@SpringBootTest(classes = WorkFlowContextSpring.class)
//@ActiveProfiles("springtest")
public abstract class TestWithMemoryDB {

	@Autowired
	private DataSource dataSource;

	@BeforeEach
	public final void setup() throws SQLException, ClassNotFoundException, IOException {
		new MemDB(dataSource).createDB();
	}

	@AfterEach
	public final void destroy() throws SQLException, ClassNotFoundException {
		new MemDB(dataSource).deleteDB();
	}
}
