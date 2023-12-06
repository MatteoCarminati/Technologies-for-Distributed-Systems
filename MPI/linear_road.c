#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

/**
 * Group number:
 *
 * Group members
 * Member 1
 * Member 2
 * Member 3
 *
 **/

// Set DEBUG 1 if you want car movement to be deterministic
#define DEBUG 0

const int num_segments = 256;

const int num_iterations = 1000;
const int count_every = 10;

const double alpha = 0.5;
const int max_in_per_sec = 10;

// Returns the number of car that enter the first segment at a given iteration.
int create_random_input() {
    if (DEBUG){
        return 1;
    } else {
        return rand() % max_in_per_sec;
    } 
}

// Returns 1 if a car needs to move to the next segment at a given iteration, 0 otherwise.
int move_next_segment() {
    if(DEBUG){
        return 1;
    } 
    return (double)rand()/RAND_MAX < alpha ? 1 : 0;
}

int main(int argc, char** argv) { 
    MPI_Init(NULL, NULL);

    int rank;
    int num_procs;
    MPI_Comm_rank(MPI_COMM_WORLD, &rank);
    MPI_Comm_size(MPI_COMM_WORLD, &num_procs);
    srand(time(NULL) + rank);

    int segs_per_procs = num_segments/num_procs;
    //rank*segs_per_procs -> (rank+1)*segs_per_procs-1
    int* segments = (int*)calloc(segs_per_procs,sizeof(int));
    
    // Simulate for num_iterations iterations
    for (int it = 0; it < num_iterations; ++it) {
        // Move cars across segments
        int cars_to_move = 0;
        for(int i=segs_per_procs-1;i>=0;i--){
            int cars = segments[i];
            for (int j=0;j<cars;j++){
                if(move_next_segment()){
                    segments[i]--;
                    if(i==segs_per_procs-1){
                        cars_to_move++;
                    } else {
                        segments[i+1]++;
                    }
                }
            }
        }
        if(rank!=num_procs-1){
            MPI_Send(&cars_to_move,1,MPI_INT,rank+1,0,MPI_COMM_WORLD);
        }
        // New cars may enter in the first segment
        if(rank==0){
            segments[0]+=create_random_input();
        } else {
            int recv = 0;
            MPI_Recv(&recv,1,MPI_INT,rank-1,0,MPI_COMM_WORLD,MPI_STATUS_IGNORE);
            segments[0]+=recv;
        }
        
        // When needed, compute the overall sum
        if (it%count_every == 0) {
            int sum=0;
            for(int i=0;i<segs_per_procs;i++){
                sum+=segments[i];
            }
            int* sum_buffer = NULL;
            if(rank==0){
                sum_buffer = (int*)calloc(num_procs,sizeof(int));
            }
            MPI_Gather(&sum,1,MPI_INT,sum_buffer,1,MPI_INT,0,MPI_COMM_WORLD);
            if (rank==0){
                int global_sum = 0;
                for(int i=0;i<num_procs;i++){
                    global_sum+=sum_buffer[i];
                    printf("Buffer: %d\n", sum_buffer[i]);
                }
                printf("Iteration: %d, sum: %d\n", it, global_sum);
            }
            free(sum_buffer);
        }
    }
      
    MPI_Barrier(MPI_COMM_WORLD);
    free(segments);
    MPI_Finalize();
}

