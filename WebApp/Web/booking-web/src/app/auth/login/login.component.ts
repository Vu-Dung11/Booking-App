import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  email = '';
  password = '';
  isLoading = signal(false);
  error = signal('');

  ngOnInit(): void {
    if (this.route.snapshot.queryParamMap.get('reason') === 'forbidden') {
      this.error.set('Phien dang nhap khong hop le. Vui long dang nhap bang tai khoan chu homestay (HOST).');
    }
  }

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
          this.router.navigate(['/host/dashboard']);
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
