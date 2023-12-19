/*
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * This file is part of the Contiki operating system.
 *
 */

#include "contiki.h"
#include "net/routing/routing.h"
#include "net/netstack.h"
#include "net/ipv6/simple-udp.h"

#include "sys/log.h"
#include "random.h"

#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_INFO

#define UDP_CLIENT_PORT 8765
#define UDP_SERVER_PORT 5678
#define MAX_RECEIVERS 2
#define MAX_READINGS 10

#define ALERT_THRESHOLD 19

static struct simple_udp_connection udp_conn;

static unsigned readings[MAX_READINGS];
static unsigned next_reading = 0;

static uip_ipaddr_t clients[MAX_RECEIVERS];
static unsigned next_rec = 0;

PROCESS(udp_server_process, "UDP server");
AUTOSTART_PROCESSES(&udp_server_process);
/*---------------------------------------------------------------------------*/
//checks if a client is already connected, and if not if it can be accepted as a new client
unsigned check_ip(const uip_ipaddr_t* sender){
    static uint8_t i=0;
    if(next_rec<MAX_RECEIVERS){
        return 0; //free space
    } else {
        for(i=0;i<MAX_RECEIVERS;i++){
            if(uip_ipaddr_cmp(&clients[i],sender)){
                return 1; //existing client
            }
        }
        return 2;
    }
}

static void
udp_rx_callback(struct simple_udp_connection *c,
                const uip_ipaddr_t *sender_addr,
                uint16_t sender_port,
                const uip_ipaddr_t *receiver_addr,
                uint16_t receiver_port,
                const uint8_t *data,
                uint16_t datalen)
{
    unsigned reading = *(unsigned*)data;
    unsigned check = check_ip(sender_addr);
    if (check==0){
        //add client
        uip_ipaddr_copy(&clients[next_rec],sender_addr);
        LOG_INFO("Client added\n");
        next_rec++;  
        readings[next_reading++] = reading;
        if (next_reading == MAX_READINGS){
            next_reading = 0;
        }

        /* Compute average */
        float average;
        unsigned sum = 0;
        unsigned no = 0;
        unsigned i = 0;
        for (i = 0; i < MAX_READINGS; i++){
            if (readings[i] != 0){
                sum = sum + readings[i];
                no++;
            }
        }
        average = ((float)sum) / no;
        LOG_INFO("Current average is %f \n", average);

        if(average>ALERT_THRESHOLD){
            for(i=0;i<next_rec;i++){
                simple_udp_sendto(&udp_conn, &average, sizeof(average), &clients[i]);
            }
        }  
    } else if (check==2){
        //client list full
        LOG_INFO("Client rejected\n");
        return;
    } else {
        readings[next_reading++] = reading;
        if (next_reading == MAX_READINGS){
            next_reading = 0;
        }

        /* Compute average */
        float average;
        unsigned sum = 0;
        unsigned no = 0;
        unsigned i = 0;
        for (i = 0; i < MAX_READINGS; i++){
            if (readings[i] != 0){
                sum = sum + readings[i];
                no++;
            }
        }
        average = ((float)sum) / no;
        LOG_INFO("Current average is %f \n", average);

        if(average>ALERT_THRESHOLD){
            for(i=0;i<next_rec;i++){
                simple_udp_sendto(&udp_conn, &average, sizeof(average), &clients[i]);
            }
        }
    }
}
/*---------------------------------------------------------------------------*/
PROCESS_THREAD(udp_server_process, ev, data)
{
    PROCESS_BEGIN();

    static uint8_t i = 0;
    /* Initialize temperature buffer */
    for (i = 0; i < next_reading; i++)
    {
        readings[i] = 0;
    }

    /* Initialize DAG root */
    NETSTACK_ROUTING.root_start();

    /* Initialize UDP connection */
    simple_udp_register(&udp_conn, UDP_SERVER_PORT, NULL,
                        UDP_CLIENT_PORT, udp_rx_callback);

    PROCESS_END();
}
/*---------------------------------------------------------------------------*/
