const { spawn } = require('child_process');
const fs = require('fs');

async function run() {
  const sqlContent = fs.readFileSync('./seed.sql', 'utf8');
  const lines = sqlContent.split('\n').filter(l => l.trim().length > 0 && !l.startsWith('BEGIN') && !l.startsWith('COMMIT'));
  
  console.log(`Starting Data Connect SQL Shell to execute ${lines.length} queries...`);

  const shell = spawn('npx', ['firebase', 'dataconnect:sql:shell', 'dataconnect', '--project', 'storebook-42b8e'], {
    cwd: process.cwd()
  });

  let currentLineIndex = 0;
  let hasError = false;
  let isDone = false;
  let currentOutput = "";

  shell.stdout.on('data', (data) => {
    currentOutput += data.toString();
    
    // Clear buffer if it contains the prompt
    if (currentOutput.includes("? Enter your SQL query (or '.exit'):")) {
        const matches = currentOutput.match(/\? Enter your SQL query \(or '\.exit'\):/g);
        
        // Sometimes multiple prompts can get buffered, we must drain the buffer and execute that many times
        currentOutput = "";
        
        if (currentLineIndex < lines.length) {
            const query = lines[currentLineIndex];
            if (currentLineIndex % 50 === 0) {
                console.log(`Progress: ${currentLineIndex}/${lines.length} queries executed...`);
            }
            shell.stdin.write(query + '\n');
            currentLineIndex++;
        } else if (!isDone) {
            isDone = true;
            console.log("All queries executed! Exiting...");
            shell.stdin.write('.exit\n');
        }
    }
    
    if (data.toString().includes("error:") || data.toString().includes("Failed executing query:") || data.toString().includes("ERROR:")) {
        console.error("ERROR FROM SHELL:", data.toString());
        hasError = true;
    }
  });

  shell.stderr.on('data', (data) => {
    console.error("STDERR:", data.toString());
  });

  shell.on('close', (code) => {
    console.log(`Shell exited with code ${code}`);
    if (hasError) {
        console.error("Migration finished but encountered errors.");
    } else {
        console.log("Migration finished successfully!");
    }
  });
}

run().catch(console.error);
