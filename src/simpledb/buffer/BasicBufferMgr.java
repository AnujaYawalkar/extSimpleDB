package simpledb.buffer;

import java.util.HashMap;

import simpledb.file.*;

/**
 * Manages the pinning and unpinning of buffers to blocks.
 * @author Edward Sciore
 *
 */
class BasicBufferMgr 
{
   private Buffer[] bufferpool;
   private int numAvailable;
   
   /*
    * Akond April 19, 2015 
    * 
    * */
   //Block block = new B
   HashMap<Block, Buffer> bufferBlockMap = new HashMap< Block, Buffer>();   
   
   /**
    * Creates a buffer manager having the specified number 
    * of buffer slots.
    * This constructor depends on both the {@link FileMgr} and
    * {@link simpledb.log.LogMgr LogMgr} objects 
    * that it gets from the class
    * {@link simpledb.server.SimpleDB}.
    * Those objects are created during system initialization.
    * Thus this constructor cannot be called until 
    * {@link simpledb.server.SimpleDB#initFileAndLogMgr(String)} or
    * is called first.
    * @param numbuffs the number of buffer slots to allocate
    */
   BasicBufferMgr(int numbuffs) {
      bufferpool = new Buffer[numbuffs];
      numAvailable = numbuffs;
      for (int i=0; i<numbuffs; i++)
         bufferpool[i] = new Buffer();
   }
   
   /**
    * Flushes the dirty buffers modified by the specified transaction.
    * @param txnum the transaction's id number
    */
   synchronized void flushAll(int txnum) {
      for (Buffer buff : bufferpool)
         if (buff.isModifiedBy(txnum))
         buff.flush();
   }
   
   /**
    * Pins a buffer to the specified block. 
    * If there is already a buffer assigned to that block
    * then that buffer is used;  
    * otherwise, an unpinned buffer from the pool is chosen.
    * Returns a null value if there are no available buffers.
    * @param blk a reference to a disk block
    * @return the pinned buffer
    */
   synchronized Buffer pin(Block blk) 
   {
      Buffer buff = findExistingBuffer(blk);
      if (buff == null) 
      {
    	 System.out.println("asi mama !"); 
         buff = chooseUnpinnedBuffer();
         if (buff == null)
         {
             return null;        	 
         }

         //buff.assignToBlock(blk);
         /*
          * Added by Akond on April 19, 2015 
          * */
         
         bufferBlockMap.put(blk, buff);
      }
      if (!buff.isPinned())
      {
          numAvailable--;    	  
      }

      buff.pin();
      return buff;
   }
   
   /**
    * Allocates a new block in the specified file, and
    * pins a buffer to it. 
    * Returns null (without allocating the block) if 
    * there are no available buffers.
    * @param filename the name of the file
    * @param fmtr a pageformatter object, used to format the new block
    * @return the pinned buffer
    */
   synchronized Buffer pinNew(String filename, PageFormatter fmtr) {
      Buffer buff = chooseUnpinnedBuffer();
      if (buff == null)
         return null;
      buff.assignToNew(filename, fmtr);
      numAvailable--;
      buff.pin();
      return buff;
   }
   
   /**
    * Unpins the specified buffer.
    * @param buff the buffer to be unpinned
    */
   synchronized void unpin(Buffer buff) 
   {
	  Block blockObj =  buff.block(); 
      buff.unpin();
      if (!buff.isPinned())
      {
          numAvailable++;    	  
      }
      
      /*
       * 
       * Added by Akond : April 19, 2015
       * 
       * */
      bufferBlockMap.remove(blockObj, buff);

   }
   
   /**
    * Returns the number of available (i.e. unpinned) buffers.
    * @return the number of available buffers
    */
   int available() {
      return numAvailable;
   }
   
   private Buffer findExistingBuffer(Block blk) 
   {
	   Buffer buffToret = null ;
	      /*
	       * 
	       * Added by Akond : April 19, 2015
	       * 
	       * */
	   
	  /* 
      for (Buffer buff : bufferpool) 
      {
         Block b = buff.block();
         if (b != null && b.equals(blk))
         {
             return buff;        	 
         }

      }
      return null;
      
      */
      if(containsMapping(blk))
      {
    	  //System.out.println("asi mama !");
    	  buffToret = getMapping(blk);
    	  
      }
      
      return buffToret ;
      
      
   }
   
   private Buffer chooseUnpinnedBuffer() {
      for (Buffer buff : bufferpool)
         if (!buff.isPinned())
         return buff;
      return null;
   }
   
   public boolean containsMapping(Block blk)
   {
	   return bufferBlockMap.containsKey(blk);
	   
   }
   
   public Buffer getMapping (Block blk)
   {
	   return bufferBlockMap.get(blk);
   }  
}
