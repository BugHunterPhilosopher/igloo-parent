package test.jpa.more.business;

import org.iglooproject.jpa.more.business.file.model.path.HashTableFileStorePathGeneratorImpl;
import org.junit.Assert;
import org.junit.Test;

public class TestHashTableFilePathGenerator {
	
	@Test
	public void testHashTableFilePathGenerator() {
		HashTableFileStorePathGeneratorImpl pathGenerator1 = new HashTableFileStorePathGeneratorImpl(1);
		
		String path1 = pathGenerator1.getFilePath("143205", "pdf");
		Assert.assertEquals("cd/143205.pdf", path1);
		
		String path2 = pathGenerator1.getFilePath("117519", "docx");
		Assert.assertEquals("36/117519.docx", path2);
	}

}
