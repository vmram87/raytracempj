package org.raytrace.viewport.impl;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import mpi.MPI;
import mpi.Request;
import mpi.Status;

import org.raytrace.scene.IScene;
import org.raytrace.vector.impl.ReferIntValue;
import org.raytrace.vector.impl.TColor;
import org.raytrace.viewport.AbstractViewPort;

public class MPIViewport extends AbstractViewPort {
	private int rank=-1;
	private int size=-1;
	private int datasize=0;
	private int locHeight=0;
	
	//protected List<Float> imageMatrix = new ArrayList<Float> ();
	private float[] imageMatrix;
	private int[] storeRcvDatasize;

	public MPIViewport(IScene scene) {
		super(scene);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean init(String[] args) {
		
		double t, t1, t2, t3, t4;
		
		MPI.Init(args);
		rank=MPI.COMM_WORLD.Rank();
		size=MPI.COMM_WORLD.Size();
		
		Request request;
		
		
		//MPI_Status status;         /* status of communication          */
		//MPI_Request request;       /* handle for pending communication */
		//MPI_Init( &argc, &argv );
		//MPI_Comm_rank( MPI_COMM_WORLD, &rank );
		
		   
		return false;
	}

	@Override
	public boolean render() {
		// TODO Auto-generated method stub
		
		
		this.locHeight = 0;
		int rem = this.height%this.size;

		if(this.rank < rem)
			this.locHeight = this.height/this.size + 1;
		else
			this.locHeight = this.height/this.size;
		
		this.datasize = this.width * this.locHeight; 
		
		
		//test error
		//if(rank == 2) 
		//	System.exit(1);
		
		//checkpoint
		if(this.rank == 0){
			
			MPI.COMM_WORLD.checkpoint();
			try {
				Thread.currentThread().sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
		
		
		
		
		System.out.println("before dataSizeCollection");
		//send and receieve the data size
		if(!dataSizeCollection())
			return false;
		
		
		
		System.out.println("after dataSizeCollection");
		
		this.imageMatrix=new float[this.datasize*3];
		
		ReferIntValue cLoad = new ReferIntValue(0);

		for(int y = rank, ly=0; y < this.height-1; y+=size, ly++)
		{
			for(int x = 0; x < this.width; x++)
			{
				//LOAD_MEASUREMENT
				cLoad .add(20);
				TColor color=this.scene.rayTrace(x, y, cLoad);
				int segment=(ly*this.width + x)*3;

				this.imageMatrix[segment]=color.getR();
				this.imageMatrix[segment + 1]=color.getG();
				this.imageMatrix[segment + 2]=color.getB();
			}
		}
		
		//data collection from all the processes
		System.out.println("before dataCollection");
		if(!dataCollection())
		{
			return false;
		}
		System.out.println("after dataCollection");
		return true;
	}
	
	@Override
	public boolean saveToIMGFile(String fileName) {
		if(this.rank==0){
			try{
				File file = new File(fileName);   
				
				int segment;
		        
				 BufferedImage bi = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);   
		 
				 //the order is different in this load balance algorithm
				 
					int[] segarray = new int [size+1];
					int[] offarray = new int [size];
			
					int rem = this.height%this.size;
			
					segarray[0] = 0;
					for(int i = 1; i <= size; i++)
					{
						if(i < rem)
							segarray[i] = this.height/this.size + 1 + segarray[i-1];
						else
							segarray[i] = this.height/this.size + segarray[i-1];
						offarray[i-1] = 0;
					}
		
					int y = 0;
					for (int i = 0; y < this.height; i = (i + 1) % size, y++) {
						int ly = segarray[i] + offarray[i];
						if (ly >= segarray[i + 1])
							continue;
						
						offarray[i]++;
						for(int x = 0; x < this.width; x++)
						{
							segment = (ly*this.width + x) * 3;
							Color color=new Color(this.imageMatrix[segment]/255, this.imageMatrix[segment+1]/255, 
									this.imageMatrix[segment+2]/255);
							bi.setRGB(x, height-1-y, color.getRGB());				        
						}
					}			    
					ImageIO.write(bi, getFileExtension(fileName), file);  		    
			}
			
			catch(Exception e){
				e.printStackTrace();
				return false;
				
			}
		}
		
		return true;
	}
	
	private boolean dataCollection(){
		
		int i, bitval, tag = 102;
		int tempdatasize = this.width * this.locHeight * 3;

		Status status;

		bitval = 1;


		while (bitval < size)
		{
			if ((bitval & rank) != 0)
			{
				MPI.COMM_WORLD.Send(this.imageMatrix,0, tempdatasize, MPI.FLOAT, (rank^bitval), tag);
				break;
			}
			else
			{
				if ((rank^bitval) < size)
				{
					MPI.COMM_WORLD.Recv(this.imageMatrix,tempdatasize, this.storeRcvDatasize[rank^bitval]*3,
						MPI.FLOAT, (rank^bitval), tag);

					tempdatasize += this.storeRcvDatasize[rank^bitval]*3;
				}
			}
			bitval = bitval<<1;
		}
		
		return true;		
	}
	
	boolean dataSizeCollection()
	{
		
		storeRcvDatasize = new int[size];		

		int i, bitval, tag = 100;

		bitval = 1;

		while (bitval < size)
		{
			if ((bitval & rank)!=0)
			{
				int []tempArray={this.datasize};
				MPI.COMM_WORLD.Send(tempArray, 0, 1, MPI.INT, (rank^bitval), tag);
				break;
			}
			else
			{
				if ((rank^bitval) < size)
				{
					MPI.COMM_WORLD.Recv(storeRcvDatasize, (rank^bitval), 1, MPI.INT, (rank^bitval), tag);
					this.datasize += storeRcvDatasize[rank^bitval];
				}
			}
			bitval = bitval<<1;
		}

		return true;
	}
	
	
	@Override
	public void viewportFinalize() {
		MPI.Finalize();		
	}

	public static void main(String[] args){
		
		
	}

}
