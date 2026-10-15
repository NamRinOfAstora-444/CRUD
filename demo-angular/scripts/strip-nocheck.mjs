// Script này để xoá các @ts-nocheck
import { readFileSync, writeFileSync } from 'fs';
import { glob } from 'fs/promises';

for await (const file of glob('src/**/*.ts')) {
  const original = readFileSync(file, 'utf8');
  const stripped = original.replace(/\/\/ @ts-nocheck\n?/g, '');
  if (stripped !== original) {
    writeFileSync(file, stripped, 'utf8');
    console.log(`Removed @ts-nocheck: ${file}`);
  }
}
