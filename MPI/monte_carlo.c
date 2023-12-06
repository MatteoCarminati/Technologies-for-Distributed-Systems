#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <mpi.h>

const int num_iter_per_proc = 10 * 1000 * 1000;

int main() {
    MPI_Init(NULL, NULL);
    
    int rank;
    int num_procs;
    int sum=0;
    
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &num_procs);

    srand(time(NULL) + rank);

    int count = 0;
    for(int i=0;i<num_iter_per_proc;i++){
        double x = (double)rand()/RAND_MAX;
        double y = (double)rand()/RAND_MAX;
        if (x*x + y*y <= 1){
            count++;
        }
    }
    
    int* buffer = NULL;
    if(rank==0){
        buffer = (int*)malloc(sizeof(int)*num_procs);
    }
    MPI_Gather(&count,1,MPI_INT,buffer,1,MPI_INT,0,MPI_COMM_WORLD);
    
    if (rank == 0) {
        for(int i=0;i<num_procs;i++){
            sum+=buffer[i];
        }
        double pi = (4.0*sum) / (num_iter_per_proc*num_procs);
        printf("Pi = %f\n", pi);
    }
      
    MPI_Finalize();
    return 0;
}
