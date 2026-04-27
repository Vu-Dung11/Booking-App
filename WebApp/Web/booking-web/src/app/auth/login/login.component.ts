import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  email = '';
  password = '';
  isLoading = signal(false);
  error = signal('');

  onSubmit(): void {
    if (!this.email || !this.password) {
      this.error.set('Vui long nhap email va mat khau');
      return;
    }

    this.isLoading.set(true);
    this.error.set('');

    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: (res) => {
        this.isLoading.set(false);
        if (res.code === 0) {
          this.router.navigate(['/admin/dashboard']);
        } else {
          this.error.set(res.message);
        }
      },
      error: (err) => {
        this.isLoading.set(false);
        this.error.set(err.error?.message || 'Dang nhap that bai. Vui long thu lai.');
      }
    });
  }
}
